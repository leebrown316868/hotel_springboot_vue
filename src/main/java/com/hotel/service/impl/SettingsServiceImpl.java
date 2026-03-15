package com.hotel.service.impl;

import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.SystemSettings;
import com.hotel.mapper.SettingsMapper;
import com.hotel.repository.SystemSettingsRepository;
import com.hotel.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SystemSettingsRepository repository;
    private final SettingsMapper mapper;

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
}
