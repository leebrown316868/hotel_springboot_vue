package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String country;
    private String status;
    private Integer totalBookings;
    private LocalDate lastStay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
