package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.BookingTrendData;
import com.hotel.dto.DashboardStatistics;
import com.hotel.dto.RecentBookingSummary;
import com.hotel.dto.RoomStatusDistribution;
import com.hotel.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取仪表板统计数据
     * 包括：总客房数、空闲客房数、入住率、今日预计入住数、今日待退房数、今日预估收入
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<DashboardStatistics> getDashboardStatistics() {
        log.info("获取仪表板统计数据");
        DashboardStatistics statistics = statisticsService.getDashboardStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取客房状态分布
     * 返回各种状态的房间数量
     */
    @GetMapping("/room-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<RoomStatusDistribution>> getRoomStatusDistribution() {
        log.info("获取客房状态分布");
        List<RoomStatusDistribution> distribution = statisticsService.getRoomStatusDistribution();
        return ApiResponse.success(distribution);
    }

    /**
     * 获取预订趋势数据
     *
     * @param days 统计天数，默认7天
     */
    @GetMapping("/booking-trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<BookingTrendData>> getBookingTrends(
            @RequestParam(defaultValue = "7") int days) {
        log.info("获取预订趋势数据，天数：{}", days);
        List<BookingTrendData> trends = statisticsService.getBookingTrends(days);
        return ApiResponse.success(trends);
    }

    /**
     * 获取最近预订列表
     *
     * @param limit 返回数量限制，默认4条
     */
    @GetMapping("/recent-bookings")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<RecentBookingSummary>> getRecentBookings(
            @RequestParam(defaultValue = "4") int limit) {
        log.info("获取最近预订列表，限制：{}", limit);
        List<RecentBookingSummary> bookings = statisticsService.getRecentBookings(limit);
        return ApiResponse.success(bookings);
    }
}
