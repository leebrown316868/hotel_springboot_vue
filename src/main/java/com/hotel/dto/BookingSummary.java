package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummary {

    private Long id;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private Boolean reviewed;
    private Integer rating;
    private String comment;
}
