package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.NotificationListResponse;
import com.hotel.dto.NotificationResponse;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String email = getEmailFromUserDetails(userDetails);
        NotificationListResponse response = notificationService.getUserNotifications(email, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = getEmailFromUserDetails(userDetails);
        Integer count = notificationService.getUnreadCount(email);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = getEmailFromUserDetails(userDetails);
        NotificationResponse response = notificationService.markAsRead(id, email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = getEmailFromUserDetails(userDetails);
        notificationService.markAllAsRead(email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String getEmailFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) userDetails).getEmail();
        } else if (userDetails instanceof com.hotel.security.GuestDetailsImpl) {
            return ((com.hotel.security.GuestDetailsImpl) userDetails).getGuest().getEmail();
        }
        return userDetails.getUsername();
    }
}
