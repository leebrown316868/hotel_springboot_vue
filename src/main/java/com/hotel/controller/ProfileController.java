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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = getEmailFromUserDetails(userDetails);
        ProfileResponse response = profileService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = getEmailFromUserDetails(userDetails);
        ProfileResponse response = profileService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = getEmailFromUserDetails(userDetails);
        profileService.changePassword(email, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String getEmailFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) userDetails).getEmail();
        } else if (userDetails instanceof com.hotel.security.GuestDetailsImpl) {
            return ((com.hotel.security.GuestDetailsImpl) userDetails).getGuest().getEmail();
        }
        return userDetails.getUsername();
    }
}
