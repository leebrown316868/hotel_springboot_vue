package com.hotel.service.impl;

import com.hotel.dto.NotificationListResponse;
import com.hotel.dto.NotificationResponse;
import com.hotel.entity.Notification;
import com.hotel.entity.NotificationType;
import com.hotel.mapper.NotificationMapper;
import com.hotel.repository.NotificationRepository;
import com.hotel.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @Override
    public NotificationListResponse getUserNotifications(String userId, Pageable pageable) {
        Page<Notification> page = repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationResponse> notifications = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return NotificationListResponse.builder()
                .notifications(notifications)
                .total(page.getTotalElements())
                .unreadCount(getUnreadCount(userId))
                .build();
    }

    @Override
    public Integer getUnreadCount(String userId) {
        return Math.toIntExact(repository.countByUserIdAndIsReadFalse(userId));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            repository.save(notification);
        }

        return mapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = repository.findByUserIdAndIsReadFalse(userId);
        LocalDateTime now = LocalDateTime.now();

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(now);
        }

        repository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(String userId, String title, String message,
                                                   NotificationType type, Integer priority, String actionLink) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPriority(priority != null ? priority : 0);
        notification.setActionLink(actionLink);

        Notification saved = repository.save(notification);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void cleanupOldNotifications(String userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        repository.deleteOldReadNotifications(userId, thirtyDaysAgo);
    }
}
