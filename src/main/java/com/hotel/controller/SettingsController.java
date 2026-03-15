package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.UserRole;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettings() {
        SettingsResponse response = settingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Secured("ROLE_ADMIN")
    public ResponseEntity<ApiResponse<SettingsResponse>> updateSettings(
            @Valid @RequestBody SettingsRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SettingsResponse response = settingsService.updateSettings(request, userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
