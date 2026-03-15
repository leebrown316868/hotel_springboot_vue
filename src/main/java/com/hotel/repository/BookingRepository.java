package com.hotel.repository;

import com.hotel.entity.Booking;
import com.hotel.entity.BookingStatus;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * 根据订单号查询预订
     */
    Optional<Booking> findByBookingNumber(String bookingNumber);

    /**
     * 根据客人ID查询预订记录（按创建时间倒序）
     */
    Page<Booking> findByGuest_IdOrderByCreatedAtDesc(Long guestId, Pageable pageable);

    /**
     * 根据状态列表查询预订
     */
    Page<Booking> findByStatusIn(List<BookingStatus> statuses, Pageable pageable);

    /**
     * 统计指定日期范围内创建的预订数量
     */
    Long countByCreatedAtBetween(LocalDate start, LocalDate end);

    /**
     * 检查房间在指定日期是否已被预订（排除已取消和已退房的记录）
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
           "WHERE b.room = :room " +
           "AND b.checkInDate < :checkOut " +
           "AND b.checkOutDate > :checkIn " +
           "AND b.status NOT IN :excludeStatuses")
    boolean existsByRoomAndCheckInDateBeforeAndCheckOutDateAfterAndStatusNotIn(
        @Param("room") Room room,
        @Param("checkOut") LocalDate checkOut,
        @Param("checkIn") LocalDate checkIn,
        @Param("excludeStatuses") List<BookingStatus> excludeStatuses
    );

    /**
     * 按订单号或客人姓名搜索预订
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "b.bookingNumber LIKE %:keyword% OR " +
           "b.guestName LIKE %:keyword%")
    Page<Booking> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询指定日期范围内可用的房间
     * 排除已被预订的房间（预订状态不是已取消或已退房）
     */
    @Query("SELECT DISTINCT r FROM Room r WHERE " +
           "r.id NOT IN (" +
           "  SELECT b.room.id FROM Booking b WHERE " +
           "  b.checkInDate < :checkOut AND " +
           "  b.checkOutDate > :checkIn AND " +
           "  b.status NOT IN :excludeStatuses" +
           ") " +
           "AND (:guestCount IS NULL OR r.capacity >= :guestCount) " +
           "AND (:roomTypes IS NULL OR r.type IN :roomTypes)")
    List<Room> findAvailableRooms(
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut,
        @Param("guestCount") Integer guestCount,
        @Param("roomTypes") List<RoomType> roomTypes,
        @Param("excludeStatuses") List<BookingStatus> excludeStatuses
    );

    /**
     * 查询指定日期范围内的所有预订（用于报表统计）
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "b.checkInDate <= :endDate AND " +
           "b.checkOutDate >= :startDate")
    List<Booking> findBookingsInDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 查询指定房间的当前有效预订
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "b.room.id = :roomId AND " +
           "b.status IN :activeStatuses")
    List<Booking> findActiveBookingsByRoom(
        @Param("roomId") Long roomId,
        @Param("activeStatuses") List<BookingStatus> activeStatuses
    );
}
