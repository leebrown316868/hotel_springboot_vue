package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 最近预订摘要DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentBookingSummary {
    /**
     * 预订编号
     */
    private String bookingNumber;

    /**
     * 客人姓名
     */
    private String guestName;

    /**
     * 房间号
     */
    private String roomNumber;

    /**
     * 房型
     */
    private String roomType;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 入住日期
     */
    private LocalDate checkInDate;
}
