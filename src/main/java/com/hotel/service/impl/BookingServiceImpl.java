package com.hotel.service.impl;

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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;
    private final RoomMapper roomMapper;

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
        if (request.getGuestInfo() != null) {
            // 如果提供了客人信息，先查找或创建客人记录
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
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        bookingRepository.save(booking);
        log.info("Cancelled booking {}", id);
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
    }

    @Override
    @Transactional
    public BookingResponse processPayment(Long id, String paymentMethod) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("预订不存在: " + id));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStatusException("只有待确认的订单可以支付");
        }

        // 模拟支付处理延迟
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(1000, 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟约5%的失败率
        if (new Random().nextDouble() < 0.05) {
            throw new PaymentFailedException("支付失败，请重试");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);

        booking = bookingRepository.save(booking);
        log.info("Payment processed for booking {}", id);

        return bookingMapper.toResponse(booking);
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
            rooms = bookingRepository.findAvailableRoomsWithTypeFilter(
                    request.getCheckInDate(),
                    request.getCheckOutDate(),
                    request.getGuestCount(),
                    request.getRoomTypes(),
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
                guest.setUpdatedAt(LocalDateTime.now());
                return guestRepository.save(guest);
            }
        }

        // 创建新客人
        Guest newGuest = Guest.builder()
                .name(guestInfo.getName() != null ? guestInfo.getName() : "未知")
                .email(guestInfo.getEmail() != null ? guestInfo.getEmail() : "unknown@temp.com")
                .phone(guestInfo.getPhone() != null ? guestInfo.getPhone() : "0000000000")
                .country("中国") // 默认国家
                .status(GuestStatus.ACTIVE)
                .totalBookings(0)
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
