package com.hotel.repository;

import com.hotel.entity.RoomTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomTypeEntity, Long> {

    Optional<RoomTypeEntity> findByCode(String code);

    @Query("SELECT rt FROM RoomTypeEntity rt WHERE rt.active = true ORDER BY rt.code ASC")
    List<RoomTypeEntity> findByActiveTrueOrderByCodeAsc();

    @Query("SELECT rt FROM RoomTypeEntity rt WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(rt.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(rt.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RoomTypeEntity> searchRoomTypes(@Param("search") String search, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.type = :typeCode")
    long countByType(@Param("typeCode") String typeCode);
}
