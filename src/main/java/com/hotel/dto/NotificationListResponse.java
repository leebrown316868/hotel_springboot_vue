package com.hotel.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private Long total;
    private Integer unreadCount;
}
