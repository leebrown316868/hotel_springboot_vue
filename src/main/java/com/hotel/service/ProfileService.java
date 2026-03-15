package com.hotel.service;

import com.hotel.dto.ProfileResponse;
import com.hotel.dto.UpdateProfileRequest;
import com.hotel.entity.User;
import com.hotel.exception.ProfileNotFoundException;
import com.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException("用户不存在"));
        return mapToProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException("用户不存在"));

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setNationality(request.getNationality());
        user.setPreferencesEnabled(request.getPreferencesEnabled());

        user = userRepository.save(user);
        return mapToProfileResponse(user);
    }

    private ProfileResponse mapToProfileResponse(User user) {
        // 将name字段拆分为firstName和lastName
        String[] nameParts = user.getName().split(" ", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .phone(user.getPhone())
                .nationality(user.getNationality())
                .preferencesEnabled(user.getPreferencesEnabled())
                .role(user.getRole().name())
                .build();
    }
}
