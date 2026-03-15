package com.hotel.repository;

import com.hotel.entity.Notification;
import com.hotel.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(String userId);

    List<Notification> findByUserIdAndIsReadFalse(String userId);

    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true AND n.readAt < :date")
    void deleteOldReadNotifications(@Param("userId") String userId, @Param("date") LocalDateTime date);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type = :type ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndType(@Param("userId") String userId, @Param("type") NotificationType type);
}
