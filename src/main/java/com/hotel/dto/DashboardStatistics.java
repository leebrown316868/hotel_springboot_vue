package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 仪表板统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatistics {
    /**
     * 总客房数
     */
    private Long totalRooms;

    /**
     * 空闲客房数
     */
    private Long availableRooms;

    /**
     * 入住率（百分比）
     */
    private Double occupancyRate;

    /**
     * 今日预计入住数
     */
    private Long todayCheckIns;

    /**
     * 今日待退房数
     */
    private Long todayCheckOuts;

    /**
     * 今日预估收入
     */
    private BigDecimal todayRevenue;
}
