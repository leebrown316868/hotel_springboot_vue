package com.hotel.entity;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * 支付状态枚举
 */
@Enumerated(EnumType.STRING)
public enum PaymentStatus {
    /**
     * 未支付
     */
    UNPAID,

    /**
     * 已支付
     */
    PAID,

    /**
     * 已退款
     */
    REFUNDED
}
