package com.hotel.entity;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * 预订状态枚举
 */
@Enumerated(EnumType.STRING)
public enum BookingStatus {
    /**
     * 待确认
     */
    PENDING,

    /**
     * 已确认
     */
    CONFIRMED,

    /**
     * 已入住
     */
    CHECKED_IN,

    /**
     * 已退房
     */
    CHECKED_OUT,

    /**
     * 已取消
     */
    CANCELLED
}
