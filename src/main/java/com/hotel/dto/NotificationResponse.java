package com.hotel.dto;

import com.hotel.entity.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private Integer priority;
    private String actionLink;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
