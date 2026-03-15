package com.hotel.repository;

import com.hotel.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBookingId(Long bookingId);

    List<Review> findByUserId(Long userId);

    boolean existsByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);
}
