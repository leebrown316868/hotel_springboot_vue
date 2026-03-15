package com.hotel.service;

import com.hotel.dto.BookingListResponse;
import com.hotel.dto.BookingRequest;
import com.hotel.dto.BookingResponse;
import com.hotel.dto.RoomSearchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {

    BookingListResponse findAll(String search, String status, Pageable pageable);

    BookingListResponse findByGuestId(Long guestId, Pageable pageable);

    BookingResponse findById(Long id);

    BookingResponse create(BookingRequest request, Long userId);

    BookingResponse updateStatus(Long id, String status);

    void cancelBooking(Long id, Long userId);

    void checkIn(Long id);

    void checkOut(Long id);

    BookingResponse processPayment(Long id, String paymentMethod);

    List<com.hotel.dto.RoomResponse> findAvailableRooms(RoomSearchRequest request);
}
