package com.hotel.mapper;

import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.SystemSettings;
import org.springframework.stereotype.Component;

@Component
public class SettingsMapper {

    public SettingsResponse toResponse(SystemSettings entity) {
        SettingsResponse response = new SettingsResponse();
        response.setId(entity.getId());
        response.setHotelName(entity.getHotelName());
        response.setContactEmail(entity.getContactEmail());
        response.setContactPhone(entity.getContactPhone());
        response.setAddress(entity.getAddress());
        response.setCurrency(entity.getCurrency());
        response.setTimezone(entity.getTimezone());
        response.setLanguage(entity.getLanguage());
        response.setTwoFactorEnabled(entity.getTwoFactorEnabled());
        response.setSessionTimeout(entity.getSessionTimeout());
        response.setPasswordExpiry(entity.getPasswordExpiry());
        response.setEmailNotificationBookings(entity.getEmailNotificationBookings());
        response.setEmailNotificationCancellations(entity.getEmailNotificationCancellations());
        response.setPushNotificationsEnabled(entity.getPushNotificationsEnabled());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        return response;
    }

    public void updateEntityFromRequest(SettingsRequest request, SystemSettings entity) {
        entity.setHotelName(request.getHotelName());
        entity.setContactEmail(request.getContactEmail());
        entity.setContactPhone(request.getContactPhone());
        entity.setAddress(request.getAddress());
        entity.setCurrency(request.getCurrency());
        entity.setTimezone(request.getTimezone());
        entity.setLanguage(request.getLanguage());
        entity.setTwoFactorEnabled(request.getTwoFactorEnabled());
        entity.setSessionTimeout(request.getSessionTimeout());
        entity.setPasswordExpiry(request.getPasswordExpiry());
        entity.setEmailNotificationBookings(request.getEmailNotificationBookings());
        entity.setEmailNotificationCancellations(request.getEmailNotificationCancellations());
        entity.setPushNotificationsEnabled(request.getPushNotificationsEnabled());
    }
}
