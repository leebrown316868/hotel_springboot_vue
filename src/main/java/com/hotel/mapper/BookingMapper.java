package com.hotel.mapper;

import com.hotel.dto.BookingListResponse;
import com.hotel.dto.BookingRequest;
import com.hotel.dto.BookingResponse;
import com.hotel.entity.Booking;
import com.hotel.entity.BookingStatus;
import com.hotel.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .guestName(booking.getGuestName())
                .roomNumber(booking.getRoom() != null ? booking.getRoom().getNumber() : null)
                .roomType(booking.getRoomType())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .guestCount(booking.getGuestCount())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .paymentStatus(booking.getPaymentStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public BookingListResponse toResponseList(Page<Booking> bookings) {
        List<BookingResponse> bookingResponses = bookings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return BookingListResponse.builder()
                .content(bookingResponses)
                .totalElements((int) bookings.getTotalElements())
                .totalPages(bookings.getTotalPages())
                .currentPage(bookings.getNumber())
                .pageSize(bookings.getSize())
                .build();
    }

    public BookingStatus parseBookingStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return BookingStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public PaymentStatus parsePaymentStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return PaymentStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
