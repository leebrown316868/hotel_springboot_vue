package com.hotel.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingReviewResponse {
    private Long id;
    private String bookingNumber;
    private String roomType;
    private String checkInDate;
    private String checkOutDate;
    private Boolean reviewed;
    private Integer rating;
    private String comment;
}
