package com.hotel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RoomTypeResponse {

    private Long id;
    private String code;
    private String name;
    private Integer capacity;
    private BigDecimal basePrice;
    private Boolean active;
    private Integer roomCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
