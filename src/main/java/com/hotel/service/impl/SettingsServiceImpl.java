package com.hotel.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.dto.PublicSettingsResponse;
import com.hotel.dto.RoomTypeConfig;
import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.SystemSettings;
import com.hotel.mapper.SettingsMapper;
import com.hotel.repository.SystemSettingsRepository;
import com.hotel.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SystemSettingsRepository repository;
    private final SettingsMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public SettingsResponse getSettings() {
        SystemSettings settings = repository.getSingleton();
        return mapper.toResponse(settings);
    }

    @Override
    @Transactional
    public SettingsResponse updateSettings(SettingsRequest request, String updatedBy) {
        SystemSettings settings = repository.getSingleton();
        mapper.updateEntityFromRequest(request, settings);
        settings.setUpdatedBy(updatedBy);
        SystemSettings saved = repository.save(settings);
        return mapper.toResponse(saved);
    }

    @Override
    public Map<String, RoomTypeConfig> getRoomTypesConfig() {
        try {
            SystemSettings settings = repository.getSingleton();
            String configJson = settings.getRoomTypesConfig();
            if (configJson == null || configJson.isEmpty()) {
                return getDefaultRoomTypesConfig();
            }
            return objectMapper.readValue(configJson, new TypeReference<Map<String, RoomTypeConfig>>() {});
        } catch (Exception e) {
            log.error("Failed to parse room types config, returning default", e);
            return getDefaultRoomTypesConfig();
        }
    }

    @Override
    @Transactional
    public Map<String, RoomTypeConfig> updateRoomTypesConfig(Map<String, RoomTypeConfig> config) {
        try {
            String configJson = objectMapper.writeValueAsString(config);
            SystemSettings settings = repository.getSingleton();
            settings.setRoomTypesConfig(configJson);
            repository.save(settings);
            log.info("Room types config updated successfully");
            return config;
        } catch (Exception e) {
            log.error("Failed to update room types config", e);
            throw new RuntimeException("Failed to update room types config: " + e.getMessage(), e);
        }
    }

    private Map<String, RoomTypeConfig> getDefaultRoomTypesConfig() {
        Map<String, RoomTypeConfig> config = new HashMap<>();
        config.put("SINGLE", new RoomTypeConfig("单人间", 1, new BigDecimal("150")));
        config.put("DOUBLE", new RoomTypeConfig("双人间", 2, new BigDecimal("220")));
        config.put("SUITE", new RoomTypeConfig("套房", 2, new BigDecimal("350")));
        config.put("EXECUTIVE_SUITE", new RoomTypeConfig("行政套房", 3, new BigDecimal("500")));
        config.put("PRESIDENTIAL_SUITE", new RoomTypeConfig("总统套房", 4, new BigDecimal("850")));
        return config;
    }

    @Override
    public PublicSettingsResponse getPublicSettings() {
        SystemSettings settings = repository.getSingleton();
        return PublicSettingsResponse.builder()
                .hotelName(settings.getHotelName())
                .description(settings.getDescription())
                .address(settings.getAddress())
                .contactPhone(settings.getContactPhone())
                .contactEmail(settings.getContactEmail())
                .build();
    }
}
