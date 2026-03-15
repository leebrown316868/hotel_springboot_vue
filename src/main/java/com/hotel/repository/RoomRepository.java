package com.hotel.repository;

import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    Optional<Room> findByNumber(String number);

    List<Room> findByFloor(String floor);

    List<Room> findByStatus(RoomStatus status);

    List<Room> findByType(RoomType type);

    List<Room> findByType(String type);

    boolean existsByNumber(String number);

    /**
     * 统计指定状态的房间数量
     */
    Long countByStatus(RoomStatus status);
}
