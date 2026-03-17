package com.hotel.service;

import com.hotel.dto.PublicSettingsResponse;
import com.hotel.dto.RoomTypeConfig;
import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public interface SettingsService {
    SettingsResponse getSettings();
    SettingsResponse updateSettings(SettingsRequest request, String updatedBy);

    Map<String, RoomTypeConfig> getRoomTypesConfig();

    @Transactional
    Map<String, RoomTypeConfig> updateRoomTypesConfig(Map<String, RoomTypeConfig> config);

    PublicSettingsResponse getPublicSettings();
}
