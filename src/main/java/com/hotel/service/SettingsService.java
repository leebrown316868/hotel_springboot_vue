package com.hotel.service;

import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;

public interface SettingsService {
    SettingsResponse getSettings();
    SettingsResponse updateSettings(SettingsRequest request, String updatedBy);
}
