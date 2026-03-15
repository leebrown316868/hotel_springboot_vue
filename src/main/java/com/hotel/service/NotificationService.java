package com.hotel.service;

import com.hotel.dto.NotificationListResponse;
import com.hotel.dto.NotificationResponse;
import com.hotel.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationListResponse getUserNotifications(String userId, Pageable pageable);
    Integer getUnreadCount(String userId);
    NotificationResponse markAsRead(Long notificationId, String userId);
    void markAllAsRead(String userId);
    NotificationResponse createNotification(String userId, String title, String message, NotificationType type, Integer priority, String actionLink);
    void cleanupOldNotifications(String userId);
}
