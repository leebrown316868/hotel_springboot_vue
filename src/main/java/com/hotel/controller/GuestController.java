package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.BookingSummary;
import com.hotel.dto.GuestListResponse;
import com.hotel.dto.GuestRequest;
import com.hotel.dto.GuestResponse;
import com.hotel.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<GuestListResponse>> getGuests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        GuestListResponse response = guestService.getGuests(page, size, search);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<GuestResponse>> getGuestById(@PathVariable Long id) {
        GuestResponse response = guestService.getGuestById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<GuestResponse>> createGuest(@Valid @RequestBody GuestRequest request) {
        GuestResponse response = guestService.createGuest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<GuestResponse>> updateGuest(
            @PathVariable Long id,
            @Valid @RequestBody GuestRequest request) {
        GuestResponse response = guestService.updateGuest(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.ok(ApiResponse.success("客户删除成功"));
    }

    @GetMapping("/{id}/bookings")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<BookingSummary>>> getGuestBookings(@PathVariable Long id) {
        List<BookingSummary> response = guestService.getGuestBookings(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
