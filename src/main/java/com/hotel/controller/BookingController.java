package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.BookingListResponse;
import com.hotel.dto.BookingRequest;
import com.hotel.dto.BookingResponse;
import com.hotel.dto.PaymentRequest;
import com.hotel.dto.RoomResponse;
import com.hotel.dto.RoomSearchRequest;
import com.hotel.entity.UserRole;
import com.hotel.service.BookingService;
import com.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

        Long userId = jwtUtil.getUserIdFromToken(authentication.getCredentials().toString());
        BookingResponse response = bookingService.create(request, userId);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("预订创建成功")
                .data(response)
                .build());
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingListResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        log.info("Fetching my bookings");

        // 从JWT中获取guestId（简化实现，假设email就是guest email）
        String email = jwtUtil.getUsernameFromToken(authentication.getCredentials().toString());

        // 这里需要根据email查找对应的guestId
        // 暂时使用固定值，实际应该从用户信息中获取
        Long guestId = 1L;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        BookingListResponse response = bookingService.findByGuestId(guestId, pageable);

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

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("Cancelling booking: {}", id);

        Long userId = jwtUtil.getUserIdFromToken(authentication.getCredentials().toString());
        bookingService.cancelBooking(id, userId);

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
}
