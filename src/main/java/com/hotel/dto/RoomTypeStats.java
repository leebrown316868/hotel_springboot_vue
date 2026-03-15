package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeStats {
    private String code;
    private Integer roomCount;
    private Integer availableCount;
    private Integer occupiedCount;
    private Integer cleaningCount;
    private Integer maintenanceCount;
}
