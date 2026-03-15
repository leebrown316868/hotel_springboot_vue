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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        NotificationListResponse response = notificationService.getUserNotifications(
                userDetails.getEmail(), pageRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Integer count = notificationService.getUnreadCount(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NotificationResponse response = notificationService.markAsRead(id, userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAllAsRead(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
