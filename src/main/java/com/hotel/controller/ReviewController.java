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
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<BookingReviewResponse> response =
                reviewService.getUserCompletedBookings(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewResponse response =
                reviewService.createReview(userDetails.getEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
