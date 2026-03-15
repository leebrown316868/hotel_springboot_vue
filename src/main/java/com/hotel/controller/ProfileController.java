package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.ChangePasswordRequest;
import com.hotel.dto.ProfileResponse;
import com.hotel.dto.UpdateProfileRequest;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ProfileResponse response = profileService.getProfile(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ProfileResponse response = profileService.updateProfile(userDetails.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        profileService.changePassword(userDetails.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
