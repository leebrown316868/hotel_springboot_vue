package com.hotel.service;

import com.hotel.dto.BookingReviewResponse;
import com.hotel.dto.ReviewRequest;
import com.hotel.dto.ReviewResponse;
import com.hotel.entity.Booking;
import com.hotel.entity.BookingStatus;
import com.hotel.entity.Guest;
import com.hotel.entity.Review;
import com.hotel.entity.User;
import com.hotel.exception.BookingNotFoundException;
import com.hotel.exception.InvalidBookingStatusException;
import com.hotel.exception.ProfileNotFoundException;
import com.hotel.exception.ReviewAlreadyExistsException;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReviewRepository;
import com.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;

    public List<BookingReviewResponse> getUserCompletedBookings(String email) {
        // 直接通过email查找Guest（注册用户都在Guest表中）
        Guest guest = guestRepository.findByEmail(email)
                .orElse(null);

        if (guest == null) {
            // 如果没有对应的Guest记录，返回空列表
            log.info("No guest found for email: {}", email);
            return List.of();
        }

        // 获取该Guest的所有已完成订单
        List<Booking> bookings = bookingRepository.findByGuest_IdAndStatusOrderByCreatedAtDesc(
                guest.getId(), BookingStatus.CHECKED_OUT);

        return bookings.stream()
                .map(this::mapToBookingReviewResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse createReview(String email, ReviewRequest request) {
        log.info("=== Creating Review ===");
        log.info("Email: {}", email);
        log.info("Booking ID: {}", request.getBookingId());
        log.info("Rating: {}", request.getRating());
        log.info("Comment length: {}", request.getComment() != null ? request.getComment().length() : 0);

        // 直接通过email查找Guest
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Guest not found for email: {}", email);
                    return new ProfileNotFoundException("用户不存在");
                });

        log.info("Guest found: id={}, email={}, name={}", guest.getId(), guest.getEmail(), guest.getName());

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> {
                    log.error("Booking not found for id: {}", request.getBookingId());
                    return new BookingNotFoundException("预订不存在");
                });

        log.info("Booking found: id={}, guest_id={}, status={}", booking.getId(), booking.getGuest().getId(), booking.getStatus());

        // 验证订单状态：只有已完成的订单才能评价
        if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
            log.error("Booking status is not CHECKED_OUT: {}", booking.getStatus());
            throw new InvalidBookingStatusException("只能对已完成的订单进行评价");
        }

        // 验证订单所有权：只能评价自己的订单
        if (!booking.getGuest().getId().equals(guest.getId())) {
            log.error("Guest ID mismatch: booking.guest_id={}, user.guest_id={}", booking.getGuest().getId(), guest.getId());
            throw new InvalidBookingStatusException("无权评价此订单");
        }

        // 检查是否已评价
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            log.error("Review already exists for booking: {}", request.getBookingId());
            throw new ReviewAlreadyExistsException("该订单已评价");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setGuest(guest);  // 设置 guest 而不是 user
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);
        log.info("Review created successfully: id={}", review.getId());
        return mapToReviewResponse(review);
    }

    private BookingReviewResponse mapToBookingReviewResponse(Booking booking) {
        Optional<Review> reviewOpt = reviewRepository.findByBookingId(booking.getId());

        return BookingReviewResponse.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .roomType(booking.getRoomType() != null ? booking.getRoomType().name() : "UNKNOWN")
                .checkInDate(booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "")
                .checkOutDate(booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "")
                .reviewed(reviewOpt.isPresent())
                .rating(reviewOpt.map(Review::getRating).orElse(null))
                .comment(reviewOpt.map(Review::getComment).orElse(null))
                .build();
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking() != null ? review.getBooking().getId() : null)
                .bookingNumber(review.getBooking() != null ? review.getBooking().getBookingNumber() : null)
                .roomType(review.getBooking() != null && review.getBooking().getRoomType() != null ? review.getBooking().getRoomType().name() : "UNKNOWN")
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .build();
    }
}
