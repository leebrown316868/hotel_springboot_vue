package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客房状态分布DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusDistribution {
    /**
     * 状态代码（AVAILABLE, OCCUPIED等）
     */
    private String status;

    /**
     * 显示名称（空闲、已入住等）
     */
    private String displayName;

    /**
     * 该状态的房间数
     */
    private Long count;
}
