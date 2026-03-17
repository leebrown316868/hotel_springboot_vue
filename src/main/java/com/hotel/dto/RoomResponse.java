package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private Long id;
    private String number;
    private String floor;
    private String type;
    private String status;
    private BigDecimal price;
    private Integer capacity;
    private String images; // JSON字符串: ["url1", "url2"]
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
