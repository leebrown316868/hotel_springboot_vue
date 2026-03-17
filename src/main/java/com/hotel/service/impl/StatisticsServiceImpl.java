package com.hotel.service.impl;

import com.hotel.dto.BookingTrendData;
import com.hotel.dto.DashboardStatistics;
import com.hotel.dto.RecentBookingSummary;
import com.hotel.dto.RoomStatusDistribution;
import com.hotel.entity.Booking;
import com.hotel.entity.BookingStatus;
import com.hotel.entity.RoomStatus;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatistics getDashboardStatistics() {
        log.debug("获取仪表板统计数据");

        LocalDate today = LocalDate.now();

        // 总客房数
        long totalRooms = roomRepository.count();

        // 空闲客房数
        long availableRooms = roomRepository.countByStatus(RoomStatus.AVAILABLE);

        // 入住率（百分比）
        double occupancyRate = 0.0;
        if (totalRooms > 0) {
            occupancyRate = ((double) (totalRooms - availableRooms) / totalRooms) * 100.0;
            occupancyRate = Math.round(occupancyRate * 100.0) / 100.0; // 保留两位小数
        }

        // 今日预计入住数（状态为已确认或已入住）
        List<BookingStatus> checkInStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);
        long todayCheckIns = bookingRepository.countTodayCheckIns(today, checkInStatuses);

        // 今日待退房数（状态为已入住且退房日期为今天）
        long todayCheckOuts = bookingRepository.countTodayCheckOuts(today, BookingStatus.CHECKED_IN);

        // 今日预估收入（入住日期为今天且已支付的订单总金额）
        BigDecimal todayRevenue = bookingRepository.sumTodayRevenue(today);
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }

        return DashboardStatistics.builder()
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .occupancyRate(occupancyRate)
                .todayCheckIns(todayCheckIns)
                .todayCheckOuts(todayCheckOuts)
                .todayRevenue(todayRevenue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomStatusDistribution> getRoomStatusDistribution() {
        log.debug("获取客房状态分布");

        List<RoomStatusDistribution> distribution = new ArrayList<>();

        for (RoomStatus status : RoomStatus.values()) {
            long count = roomRepository.countByStatus(status);
            distribution.add(RoomStatusDistribution.builder()
                    .status(status.name())
                    .displayName(status.getDisplayName())
                    .count(count)
                    .build());
        }

        return distribution;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingTrendData> getBookingTrends(int days) {
        log.debug("获取最近{}天预订趋势", days);

        List<BookingTrendData> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        // 获取统计数据
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

        List<Object[]> results = bookingRepository.countBookingsByDate(startDateTime, endDateTime);

        // 转换为Map便于查找
        java.util.Map<String, Long> countMap = new java.util.HashMap<>();
        for (Object[] result : results) {
            if (result[0] != null) {
                String date = result[0].toString();
                Long count = result[1] != null ? ((Number) result[1]).longValue() : 0L;
                countMap.put(date, count);
            }
        }

        // 填充所有日期（包括没有预订的日期）
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateKey = date.toString();
            String displayDate = date.format(formatter);

            long count = countMap.getOrDefault(dateKey, 0L);
            trends.add(BookingTrendData.builder()
                    .date(displayDate)
                    .count(count)
                    .build());
        }

        return trends;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecentBookingSummary> getRecentBookings(int limit) {
        log.debug("获取最近{}条预订", limit);

        // 获取最近的预订记录（按创建时间倒序）
        List<Booking> bookings = bookingRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, limit,
                    org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
        ).getContent();

        List<RecentBookingSummary> summaries = new ArrayList<>();
        for (Booking booking : bookings) {
            summaries.add(RecentBookingSummary.builder()
                    .bookingNumber(booking.getBookingNumber())
                    .guestName(booking.getGuestName())
                    .roomNumber(booking.getRoom() != null ? booking.getRoom().getNumber() : "")
                    .roomType(booking.getRoomType() != null ? booking.getRoomType().getDisplayName() : "")
                    .status(booking.getStatus().name())
                    .checkInDate(booking.getCheckInDate())
                    .build());
        }

        return summaries;
    }
}
