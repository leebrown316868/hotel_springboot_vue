package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.BookingReviewResponse;
import com.hotel.dto.ReviewRequest;
import com.hotel.dto.ReviewResponse;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingReviewResponse>>> getMyCompletedBookings(
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<List<BookingReviewResponse>>builder()
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<List<BookingReviewResponse>>builder()
                            .code(401)
                            .message("请先登录")
                            .data(null)
                            .build());
        }

        List<BookingReviewResponse> response = reviewService.getUserCompletedBookings(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        // 添加调试日志
        System.out.println("=== Review Request Debug ===");
        System.out.println("BookingId: " + request.getBookingId());
        System.out.println("Rating: " + request.getRating());
        System.out.println("Comment: " + request.getComment());
        System.out.println("Comment length: " + (request.getComment() != null ? request.getComment().length() : 0));

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<ReviewResponse>builder()
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<ReviewResponse>builder()
                            .code(401)
                            .message("请先登录")
                            .data(null)
                            .build());
        }

        ReviewResponse response = reviewService.createReview(email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
