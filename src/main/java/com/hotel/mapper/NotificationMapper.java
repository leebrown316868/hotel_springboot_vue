package com.hotel.mapper;

import com.hotel.dto.NotificationResponse;
import com.hotel.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification entity) {
        NotificationResponse response = new NotificationResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setMessage(entity.getMessage());
        response.setType(entity.getType());
        response.setIsRead(entity.getIsRead());
        response.setPriority(entity.getPriority());
        response.setActionLink(entity.getActionLink());
        response.setCreatedAt(entity.getCreatedAt());
        response.setReadAt(entity.getReadAt());
        return response;
    }
}
