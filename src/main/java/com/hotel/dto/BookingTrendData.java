package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预订趋势数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingTrendData {
    /**
     * 日期（MM-dd格式）
     */
    private String date;

    /**
     * 当天预订数
     */
    private Long count;
}
