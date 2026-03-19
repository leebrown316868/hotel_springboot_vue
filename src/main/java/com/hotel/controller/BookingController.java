package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.BookingListResponse;
import com.hotel.dto.BookingRequest;
import com.hotel.dto.BookingResponse;
import com.hotel.dto.PaymentRequest;
import com.hotel.dto.RoomResponse;
import com.hotel.dto.RoomSearchRequest;
import com.hotel.entity.Guest;
import com.hotel.entity.UserRole;
import com.hotel.repository.GuestRepository;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.BookingService;
import com.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtil jwtUtil;
    private final GuestRepository guestRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(LocalDate.parse(text));
            }
        });
    }

    @GetMapping("/available-rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> searchAvailableRooms(@Valid RoomSearchRequest request) {
        log.info("Searching available rooms: {}", request);
        List<RoomResponse> rooms = bookingService.findAvailableRooms(request);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        log.info("Creating booking: {}", request);

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User not authenticated for booking creation");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<BookingResponse>builder()
                            .code(401)
                            .message("请先登录")
                            .data(null)
                            .build());
        }

        // 获取用户ID，支持 UserDetailsImpl 和 GuestDetailsImpl
        Long userId;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            userId = ((UserDetailsImpl) principal).getId();
        } else if (principal instanceof com.hotel.security.GuestDetailsImpl) {
            userId = ((com.hotel.security.GuestDetailsImpl) principal).getGuest().getId();
        } else {
            log.warn("Unknown principal type: {}", principal.getClass().getName());
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<BookingResponse>builder()
                            .code(401)
                            .message("请先登录")
                            .data(null)
                            .build());
        }

        BookingResponse response = bookingService.create(request, userId);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("预订创建成功")
                .data(response)
                .build());
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<BookingListResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<BookingListResponse>builder()
                            .code(401)
                            .message("请先登录")
                            .data(null)
                            .build());
        }

        // 获取用户邮箱，支持 UserDetailsImpl 和 GuestDetailsImpl
        String email;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            email = ((UserDetailsImpl) principal).getEmail();
        } else if (principal instanceof com.hotel.security.GuestDetailsImpl) {
            email = ((com.hotel.security.GuestDetailsImpl) principal).getGuest().getEmail();
        } else {
            log.warn("Unknown principal type: {}", principal.getClass().getName());
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<BookingListResponse>builder()
                            .code(401)
                            .message("请先登录")
                            .data(null)
                            .build());
        }

        log.info("Fetching my bookings for user: {}", email);

        // 根据用户email查找对应的guest
        Optional<Guest> guestOpt = guestRepository.findByEmail(email);

        if (guestOpt.isEmpty()) {
            // 用户没有对应的guest记录，返回空列表
            log.info("No guest found for user: {}, returning empty booking list", email);
            BookingListResponse emptyResponse = new BookingListResponse();
            emptyResponse.setContent(List.of());
            emptyResponse.setTotalElements(0);
            emptyResponse.setTotalPages(0);
            emptyResponse.setPageSize(size);
            emptyResponse.setCurrentPage(page);
            return ResponseEntity.ok(ApiResponse.success(emptyResponse));
        }

        Long guestId = guestOpt.get().getId();
        log.info("Found guest for email {}: id={}, name={}", email, guestId, guestOpt.get().getName());

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        BookingListResponse response = bookingService.findByGuestId(guestId, pageable);

        log.info("Returning {} bookings for guest_id={}", response.getContent().size(), guestId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<BookingListResponse>> getAllBookings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all bookings with search: {}, status: {}", search, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        BookingListResponse response = bookingService.findAll(search, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingDetail(@PathVariable Long id) {
        log.info("Fetching booking detail for id: {}", id);
        BookingResponse response = bookingService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{bookingNumber}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByNumber(@PathVariable String bookingNumber) {
        log.info("Fetching booking detail for number: {}", bookingNumber);
        BookingResponse response = bookingService.findByBookingNumber(bookingNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        log.info("Cancelling booking: {}", id);

        Long userId;
        if (userDetails instanceof UserDetailsImpl) {
            userId = ((UserDetailsImpl) userDetails).getId();
        } else if (userDetails instanceof com.hotel.security.GuestDetailsImpl) {
            userId = ((com.hotel.security.GuestDetailsImpl) userDetails).getGuest().getId();
        } else {
            throw new RuntimeException("Unknown user principal type");
        }
        
        bookingService.cancelBooking(id, userId);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("预订已取消")
                .data(null)
                .build());
    }

    @PatchMapping("/number/{bookingNumber}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBookingByNumber(
            @PathVariable String bookingNumber,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        log.info("Cancelling booking: {}", bookingNumber);

        bookingService.cancelBookingByNumber(bookingNumber);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("预订已取消")
                .data(null)
                .build());
    }

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<BookingResponse>> checkIn(@PathVariable Long id) {
        log.info("Checking in booking: {}", id);
        bookingService.checkIn(id);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("入住办理成功")
                .data(null)
                .build());
    }

    @PatchMapping("/number/{bookingNumber}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<BookingResponse>> checkInByNumber(@PathVariable String bookingNumber) {
        log.info("Checking in booking: {}", bookingNumber);
        bookingService.checkInByNumber(bookingNumber);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("入住办理成功")
                .data(null)
                .build());
    }

    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<BookingResponse>> checkOut(@PathVariable Long id) {
        log.info("Checking out booking: {}", id);
        bookingService.checkOut(id);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("退房办理成功")
                .data(null)
                .build());
    }

    @PatchMapping("/number/{bookingNumber}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<BookingResponse>> checkOutByNumber(@PathVariable String bookingNumber) {
        log.info("Checking out booking: {}", bookingNumber);
        bookingService.checkOutByNumber(bookingNumber);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("退房办理成功")
                .data(null)
                .build());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<BookingResponse>> processPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        log.info("Processing payment for booking: {} with method: {}", id, request.getPaymentMethod());

        BookingResponse response = bookingService.processPayment(id, request.getPaymentMethod());

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("支付成功")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> deleteBooking(@PathVariable Long id) {
        log.info("Deleting booking: {}", id);
        bookingService.deleteBooking(id);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("订单已删除")
                .data(null)
                .build());
    }

    @DeleteMapping("/number/{bookingNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> deleteBookingByNumber(@PathVariable String bookingNumber) {
        log.info("Deleting booking: {}", bookingNumber);
        bookingService.deleteBookingByNumber(bookingNumber);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("订单已删除")
                .data(null)
                .build());
    }
}
