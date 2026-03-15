package com.hotel.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long bookingId;
    private String bookingNumber;
    private String roomType;
    private Integer rating;
    private String comment;
    private String createdAt;
}
