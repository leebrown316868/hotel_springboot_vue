package com.hotel.service;

import com.hotel.dto.BookingTrendData;
import com.hotel.dto.DashboardStatistics;
import com.hotel.dto.RecentBookingSummary;
import com.hotel.dto.RoomStatusDistribution;

import java.util.List;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取仪表板统计数据
     * 包括：总客房数、空闲客房数、入住率、今日预计入住数、今日待退房数、今日预估收入
     *
     * @return 仪表板统计数据
     */
    DashboardStatistics getDashboardStatistics();

    /**
     * 获取客房状态分布数据
     *
     * @return 客房状态分布列表
     */
    List<RoomStatusDistribution> getRoomStatusDistribution();

    /**
     * 获取预订趋势数据
     *
     * @param days 统计天数（默认7天）
     * @return 预订趋势数据列表
     */
    List<BookingTrendData> getBookingTrends(int days);

    /**
     * 获取指定日期范围的预订趋势数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预订趋势数据列表
     */
    List<BookingTrendData> getBookingTrendsByRange(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * 获取最近预订摘要列表
     *
     * @param limit 返回数量限制（默认4条）
     * @return 最近预订摘要列表
     */
    List<RecentBookingSummary> getRecentBookings(int limit);
}
