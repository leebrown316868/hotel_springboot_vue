package com.hotel.repository;

import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    Optional<Room> findByNumber(String number);

    List<Room> findByFloor(String floor);

    List<Room> findByStatus(RoomStatus status);

    List<Room> findByType(RoomType type);

    // Query by enum name (stored in database as 'SINGLE', 'DOUBLE', etc.)
    @Query(value = "SELECT * FROM rooms WHERE type = :typeName", nativeQuery = true)
    List<Room> findByTypeName(@Param("typeName") String typeName);

    boolean existsByNumber(String number);

    /**
     * 统计指定状态的房间数量
     */
    Long countByStatus(RoomStatus status);

    /**
     * 统计指定类型的房间数量
     */
    @Query(value = "SELECT COUNT(*) FROM rooms WHERE type = :typeName", nativeQuery = true)
    Long countByType(@Param("typeName") String typeName);

    /**
     * 批量更新指定房型所有房间的价格
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE rooms SET price = :price, updated_at = NOW() WHERE type = :typeName", nativeQuery = true)
    void updatePriceByType(@Param("typeName") String typeName, @Param("price") java.math.BigDecimal price);

    /**
     * 批量更新指定房型所有房间的容量
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE rooms SET capacity = :capacity, updated_at = NOW() WHERE type = :typeName", nativeQuery = true)
    void updateCapacityByType(@Param("typeName") String typeName, @Param("capacity") Integer capacity);
}
