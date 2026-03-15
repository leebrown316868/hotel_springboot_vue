package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.RoomTypeConfig;
import com.hotel.dto.RoomTypeStats;
import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.UserRole;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.RoomService;
import com.hotel.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final RoomService roomService;

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

    @GetMapping("/room-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, RoomTypeConfig>>> getRoomTypesConfig() {
        Map<String, RoomTypeConfig> config = settingsService.getRoomTypesConfig();
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PutMapping("/room-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, RoomTypeConfig>>> updateRoomTypesConfig(
            @Valid @RequestBody Map<String, RoomTypeConfig> config) {
        Map<String, RoomTypeConfig> updated = settingsService.updateRoomTypesConfig(config);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/room-types/{code}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<RoomTypeStats>> getRoomTypeStats(@PathVariable String code) {
        RoomTypeStats stats = roomService.getRoomTypeStats(code);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
