package com.hotel.service.impl;

import com.hotel.config.AlipayConfig;
import com.hotel.dto.BookingListResponse;
import com.hotel.dto.BookingRequest;
import com.hotel.dto.BookingResponse;
import com.hotel.dto.RoomResponse;
import com.hotel.dto.RoomSearchRequest;
import com.hotel.entity.Booking;
import com.hotel.entity.BookingStatus;
import com.hotel.entity.Guest;
import com.hotel.entity.GuestStatus;
import com.hotel.entity.PaymentStatus;
import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.exception.BookingNotFoundException;
import com.hotel.exception.GuestNotFoundException;
import com.hotel.exception.InvalidBookingStatusException;
import com.hotel.exception.PaymentFailedException;
import com.hotel.exception.RoomNotFoundException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.mapper.BookingMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.BookingService;
import com.hotel.util.NotificationHelper;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;
    private final RoomMapper roomMapper;
    private final NotificationHelper notificationHelper;
    private final AlipayConfig alipayConfig;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public BookingListResponse findAll(String search, String status, Pageable pageable) {
        Page<Booking> bookings;

        if (search != null && !search.trim().isEmpty()) {
            bookings = bookingRepository.searchByKeyword(search, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            BookingStatus bookingStatus = bookingMapper.parseBookingStatus(status);
            if (bookingStatus != null) {
                bookings = bookingRepository.findByStatusIn(List.of(bookingStatus), pageable);
            } else {
                bookings = Page.empty(pageable);
            }
        } else {
            bookings = bookingRepository.findAll(pageable);
        }

        return bookingMapper.toResponseList(bookings);
    }

    @Override
    public BookingListResponse findByGuestId(Long guestId, Pageable pageable) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new GuestNotFoundException("客户不存在: " + guestId));

        Page<Booking> bookings = bookingRepository.findByGuest_IdOrderByCreatedAtDesc(guestId, pageable);
        return bookingMapper.toResponseList(bookings);
    }

    @Override
    public BookingResponse findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + id));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse create(BookingRequest request, Long userId) {
        // 验证日期
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("入住日期不能早于今天");
        }
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new IllegalArgumentException("退房日期必须晚于入住日期");
        }

        // 处理客人信息
        Guest guest;
        if (userId != null) {
            // 如果有用户ID（登录用户），优先使用用户对应的Guest记录
            guest = guestRepository.findById(userId)
                    .orElseThrow(() -> new GuestNotFoundException("客户不存在: " + userId));
            // 如果提供了guestInfo，更新guest信息（保留password和role）
            if (request.getGuestInfo() != null) {
                updateGuestInfo(guest, request.getGuestInfo());
            }
        } else if (request.getGuestInfo() != null) {
            // 未登录用户，根据客人信息查找或创建
            guest = findOrCreateGuest(request.getGuestInfo());
        } else if (request.getGuestId() != null) {
            // 使用提供的客人ID
            guest = guestRepository.findById(request.getGuestId())
                    .orElseThrow(() -> new GuestNotFoundException("客户不存在: " + request.getGuestId()));
        } else {
            throw new IllegalArgumentException("必须提供客人ID或客人信息");
        }

        // 查询房间
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("房间不存在: " + request.getRoomId()));

        // 检查房间是否可用
        if (!isRoomAvailable(room, request.getCheckInDate(), request.getCheckOutDate())) {
            throw new RoomNotAvailableException("房间在指定日期不可用");
        }

        // 计算总价
        long nights = java.time.temporal.ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalAmount = room.getPrice().multiply(BigDecimal.valueOf(nights));

        // 生成订单编号
        String bookingNumber = generateBookingNumber();

        // 创建预订
        Booking booking = Booking.builder()
                .bookingNumber(bookingNumber)
                .guest(guest)
                .guestName(guest.getName())
                .room(room)
                .roomType(room.getType())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .guestCount(request.getGuestCount())
                .status(BookingStatus.PENDING)
                .totalAmount(totalAmount)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Created booking with number: {}", bookingNumber);

        notificationHelper.notifyBookingCreated(booking);

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + id));

        BookingStatus newStatus = bookingMapper.parseBookingStatus(status);
        if (newStatus == null) {
            throw new IllegalArgumentException("无效的订单状态: " + status);
        }

        // 验证状态流转
        if (!isValidStatusTransition(booking.getStatus(), newStatus)) {
            throw new InvalidBookingStatusException(
                    "不能从 " + booking.getStatus() + " 转换到 " + newStatus);
        }

        booking.setStatus(newStatus);
        booking = bookingRepository.save(booking);

        log.info("Updated booking {} status to {}", id, newStatus);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + id));

        if (booking.getStatus() != BookingStatus.PENDING &&
                booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("只有待确认或已确认的订单可以取消");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            alipayRefund(booking.getBookingNumber(), booking.getTotalAmount());
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        bookingRepository.save(booking);
        log.info("Cancelled booking {}", id);

        notificationHelper.notifyBookingCancelled(booking);
    }

    @Override
    @Transactional
    public void checkIn(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + id));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("只有已确认的订单可以办理入住");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);

        // 更新房间状态
        Room room = booking.getRoom();
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        bookingRepository.save(booking);
        log.info("Checked in booking {}", id);

        notificationHelper.notifyCheckIn(booking);
    }

    @Override
    @Transactional
    public void checkOut(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + id));

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new InvalidBookingStatusException("只有已入住的订单可以办理退房");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);

        // 更新房间状态
        Room room = booking.getRoom();
        room.setStatus(RoomStatus.CLEANING);
        roomRepository.save(room);

        bookingRepository.save(booking);
        log.info("Checked out booking {}", id);

        notificationHelper.notifyCheckOut(booking);
    }

    @Override
    @Transactional
    public BookingResponse processPayment(Long id, String paymentMethod) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + id));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStatusException("只有待确认的订单可以支付");
        }

        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGateway(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    "UTF-8",
                    alipayConfig.getAlipayPublicKey(),
                    "RSA2"
            );

            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());

            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + booking.getBookingNumber() + "\"," +
                    "\"total_amount\":\"" + booking.getTotalAmount().toPlainString() + "\"," +
                    "\"subject\":\"酒店预订-" + booking.getRoom().getNumber() + "\"," +
                    "\"timeout_express\":\"15m\"" +
                    "}");

            AlipayTradePrecreateResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                log.info("支付宝预付订单创建成功, bookingNumber={}, qrCode={}", booking.getBookingNumber(), response.getQrCode());
                // 支付宝返回二维码，等回调更新状态
                // 这里不直接改状态，等异步回调处理
                return BookingResponse.builder()
                        .id(booking.getId())
                        .bookingNumber(booking.getBookingNumber())
                        .guestName(booking.getGuestName())
                        .roomNumber(booking.getRoom() != null ? booking.getRoom().getNumber() : "")
                        .roomType(booking.getRoomType())
                        .checkInDate(booking.getCheckInDate())
                        .checkOutDate(booking.getCheckOutDate())
                        .guestCount(booking.getGuestCount())
                        .status(booking.getStatus())
                        .totalAmount(booking.getTotalAmount())
                        .paymentStatus(booking.getPaymentStatus())
                        .createdAt(booking.getCreatedAt())
                        .qrCode(response.getQrCode())
                        .build();
            } else {
                log.error("支付宝预付订单创建失败: code={}, msg={}, subCode={}, subMsg={}",
                        response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
                throw new PaymentFailedException("创建支付订单失败: " + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("支付宝API调用异常", e);
            throw new PaymentFailedException("支付服务暂时不可用，请重试");
        }
    }

    /**
     * 处理支付宝异步回调通知
     */
    @Transactional
    public boolean handleAlipayNotify(Map<String, String> params) {
        try {
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");

            log.info("收到支付宝回调: tradeStatus={}, outTradeNo={}, tradeNo={}", tradeStatus, outTradeNo, tradeNo);

            Booking booking = bookingRepository.findByBookingNumber(outTradeNo).orElse(null);
            if (booking == null) {
                log.warn("回调对应的预订不存在: {}", outTradeNo);
                return false;
            }

            // 只处理支付成功，避免重复处理
            if (("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))
                    && booking.getPaymentStatus() != PaymentStatus.PAID) {
                booking.setPaymentStatus(PaymentStatus.PAID);
                booking.setStatus(BookingStatus.CONFIRMED);
                booking = bookingRepository.save(booking);
                log.info("支付宝回调处理成功，预订 {} 已更新为已支付", outTradeNo);
                notificationHelper.notifyPaymentConfirmed(booking);
            }

            return true;
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return false;
        }
    }

    /**
     * 调用支付宝退款API
     */
    private void alipayRefund(String bookingNumber, BigDecimal amount) {
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGateway(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    "UTF-8",
                    alipayConfig.getAlipayPublicKey(),
                    "RSA2"
            );

            com.alipay.api.request.AlipayTradeRefundRequest request = new com.alipay.api.request.AlipayTradeRefundRequest();
            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + bookingNumber + "\"," +
                    "\"refund_amount\":\"" + amount.toPlainString() + "\"," +
                    "\"out_request_no\":\"refund_" + bookingNumber + "\"" +
                    "}");

            com.alipay.api.response.AlipayTradeRefundResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                log.info("支付宝退款成功: bookingNumber={}, amount={}", bookingNumber, amount);
            } else {
                log.error("支付宝退款失败: bookingNumber={}, code={}, msg={}, subMsg={}",
                        bookingNumber, response.getCode(), response.getMsg(), response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("支付宝退款API调用异常: bookingNumber={}", bookingNumber, e);
        }
    }

    @Override
    public List<RoomResponse> findAvailableRooms(RoomSearchRequest request) {
        // 验证日期
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("入住日期不能早于今天");
        }
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new IllegalArgumentException("退房日期必须晚于入住日期");
        }

        List<Room> rooms;
        List<BookingStatus> excludeStatuses = List.of(BookingStatus.CANCELLED, BookingStatus.CHECKED_OUT);

        // 根据是否有房型筛选调用不同的查询方法
        if (request.getRoomTypes() == null || request.getRoomTypes().isEmpty()) {
            rooms = bookingRepository.findAvailableRoomsNoTypeFilter(
                    request.getCheckInDate(),
                    request.getCheckOutDate(),
                    request.getGuestCount(),
                    excludeStatuses
            );
        } else {
            List<com.hotel.entity.RoomType> enumRoomTypes = request.getRoomTypes().stream()
                    .map(com.hotel.entity.RoomType::valueOf)
                    .collect(java.util.stream.Collectors.toList());

            // 使用 JPQL 查询，直接传递 LocalDate 和 excludeStatuses
            rooms = bookingRepository.findAvailableRoomsWithTypeFilter(
                    request.getCheckInDate(),
                    request.getCheckOutDate(),
                    request.getGuestCount(),
                    enumRoomTypes,
                    excludeStatuses
            );
        }

        return rooms.stream()
                .map(roomMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 生成订单编号
     * 格式：BK-YYYYMMDD-序号
     */
    private String generateBookingNumber() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        Long count = bookingRepository.countByCreatedAtBetween(
                LocalDateTime.now().toLocalDate().atStartOfDay(),
                LocalDateTime.now().toLocalDate().plusDays(1).atStartOfDay());

        if (count == null) {
            count = 0L;
        }

        String sequence = String.format("%03d", count + 1);
        return "BK-" + today.replace("-", "") + "-" + sequence;
    }

    /**
     * 检查房间是否可用
     */
    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        boolean isBooked = bookingRepository.existsByRoomAndCheckInDateBeforeAndCheckOutDateAfterAndStatusNotIn(
                room,
                checkOut,
                checkIn,
                List.of(BookingStatus.CANCELLED, BookingStatus.CHECKED_OUT)
        );
        return !isBooked;
    }

    /**
     * 更新客人信息（保留password和role等关键字段）
     */
    private void updateGuestInfo(Guest guest, BookingRequest.GuestInfo guestInfo) {
        if (guestInfo.getName() != null) {
            guest.setName(guestInfo.getName());
        }
        if (guestInfo.getPhone() != null) {
            guest.setPhone(guestInfo.getPhone());
        }
        if (guestInfo.getCountry() != null) {
            guest.setCountry(guestInfo.getCountry());
        }
        guest.setUpdatedAt(LocalDateTime.now());
        guestRepository.save(guest);
    }

    /**
     * 查找或创建客人记录
     * 如果邮箱已存在，更新客人信息；否则创建新客人
     */
    private Guest findOrCreateGuest(BookingRequest.GuestInfo guestInfo) {
        // 如果提供了邮箱，先查找是否存在
        if (guestInfo.getEmail() != null && !guestInfo.getEmail().trim().isEmpty()) {
            java.util.Optional<Guest> existingGuest = guestRepository.findByEmail(guestInfo.getEmail());
            if (existingGuest.isPresent()) {
                // 更新现有客人信息
                Guest guest = existingGuest.get();
                if (guestInfo.getName() != null) {
                    guest.setName(guestInfo.getName());
                }
                if (guestInfo.getPhone() != null) {
                    guest.setPhone(guestInfo.getPhone());
                }
                if (guestInfo.getCountry() != null) {
                    guest.setCountry(guestInfo.getCountry());
                }
                guest.setUpdatedAt(LocalDateTime.now());
                return guestRepository.save(guest);
            }
        }

        // 创建新客人
        Guest newGuest = Guest.builder()
                .name(guestInfo.getName() != null ? guestInfo.getName() : "未知")
                .email(guestInfo.getEmail() != null ? guestInfo.getEmail() : "unknown@temp.com")
                .phone(guestInfo.getPhone() != null ? guestInfo.getPhone() : "0000000000")
                .country(guestInfo.getCountry() != null ? guestInfo.getCountry() : "中国")
                .status(GuestStatus.ACTIVE)
                .totalBookings(0)
                .role(com.hotel.entity.UserRole.CUSTOMER)
                .build();
        return guestRepository.save(newGuest);
    }

    /**
     * 验证状态流转是否合法
     */
    private boolean isValidStatusTransition(BookingStatus from, BookingStatus to) {
        if (from == BookingStatus.PENDING) {
            return to == BookingStatus.CONFIRMED || to == BookingStatus.CANCELLED;
        } else if (from == BookingStatus.CONFIRMED) {
            return to == BookingStatus.CHECKED_IN || to == BookingStatus.CANCELLED;
        } else if (from == BookingStatus.CHECKED_IN) {
            return to == BookingStatus.CHECKED_OUT;
        } else {
            return false;
        }
    }

    @Override
    public BookingResponse findByBookingNumber(String bookingNumber) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + bookingNumber));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public void cancelBookingByNumber(String bookingNumber) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + bookingNumber));

        if (booking.getStatus() != BookingStatus.PENDING &&
                booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("只有待确认或已确认的订单可以取消");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            alipayRefund(booking.getBookingNumber(), booking.getTotalAmount());
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        bookingRepository.save(booking);
        log.info("Cancelled booking {}", bookingNumber);
    }

    @Override
    @Transactional
    public void checkInByNumber(String bookingNumber) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + bookingNumber));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("只有已确认的订单可以办理入住");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);

        // 更新房间状态
        Room room = booking.getRoom();
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        bookingRepository.save(booking);
        log.info("Checked in booking {}", bookingNumber);
    }

    @Override
    @Transactional
    public void checkOutByNumber(String bookingNumber) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + bookingNumber));

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new InvalidBookingStatusException("只有已入住的订单可以办理退房");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);

        // 更新房间状态
        Room room = booking.getRoom();
        room.setStatus(RoomStatus.CLEANING);
        roomRepository.save(room);

        bookingRepository.save(booking);
        log.info("Checked out booking {}", bookingNumber);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + id));

        // 只有已取消或已退房的订单可以删除
        if (booking.getStatus() != BookingStatus.CANCELLED &&
                booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new InvalidBookingStatusException("只有已取消或已退房的订单可以删除");
        }

        bookingRepository.deleteById(id);
        log.info("Deleted booking {}", id);
    }

    @Override
    @Transactional
    public void deleteBookingByNumber(String bookingNumber) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + bookingNumber));

        // 只有已取消或已退房的订单可以删除
        if (booking.getStatus() != BookingStatus.CANCELLED &&
                booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new InvalidBookingStatusException("只有已取消或已退房的订单可以删除");
        }

        bookingRepository.delete(booking);
        log.info("Deleted booking {}", bookingNumber);
    }
}
