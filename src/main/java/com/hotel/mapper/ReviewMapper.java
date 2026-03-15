package com.hotel.mapper;

import com.hotel.dto.BookingReviewResponse;
import com.hotel.dto.ReviewResponse;
import com.hotel.entity.Booking;
import com.hotel.entity.Review;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReviewMapper {

    public ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .bookingNumber(review.getBooking().getBookingNumber())
                .roomType(review.getBooking().getRoomType().name())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt().toString())
                .build();
    }

    public BookingReviewResponse toBookingReviewResponse(Booking booking, Optional<Review> review) {
        return BookingReviewResponse.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .roomType(booking.getRoomType().name())
                .checkInDate(booking.getCheckInDate().toString())
                .checkOutDate(booking.getCheckOutDate().toString())
                .reviewed(review.isPresent())
                .rating(review.map(Review::getRating).orElse(null))
                .comment(review.map(Review::getComment).orElse(null))
                .build();
    }
}
