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
        // 通过email查找User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException("用户不存在"));

        // 通过email查找对应的Guest（假设User和Guest使用相同的email）
        Guest guest = guestRepository.findByEmail(email)
                .orElse(null);

        if (guest == null) {
            // 如果没有对应的Guest记录，返回空列表
            log.info("No guest found for user email: {}", email);
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
        // 通过email查找User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException("用户不存在"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("预订不存在"));

        // 验证订单状态：只有已完成的订单才能评价
        if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new InvalidBookingStatusException("只能对已完成的订单进行评价");
        }

        // 验证订单所有权：只能评价自己的订单
        // 通过User email查找对应的Guest
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException("无法找到对应的客户信息"));

        if (!booking.getGuest().getId().equals(guest.getId())) {
            throw new InvalidBookingStatusException("无权评价此订单");
        }

        // 检查是否已评价
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new ReviewAlreadyExistsException("该订单已评价");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);
        return mapToReviewResponse(review);
    }

    private BookingReviewResponse mapToBookingReviewResponse(Booking booking) {
        Optional<Review> reviewOpt = reviewRepository.findByBookingId(booking.getId());

        return BookingReviewResponse.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .roomType(booking.getRoomType().name())
                .checkInDate(booking.getCheckInDate().toString())
                .checkOutDate(booking.getCheckOutDate().toString())
                .reviewed(reviewOpt.isPresent())
                .rating(reviewOpt.map(Review::getRating).orElse(null))
                .comment(reviewOpt.map(Review::getComment).orElse(null))
                .build();
    }

    private ReviewResponse mapToReviewResponse(Review review) {
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
}
