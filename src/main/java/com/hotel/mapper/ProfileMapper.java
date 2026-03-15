package com.hotel.mapper;

import com.hotel.dto.ProfileResponse;
import com.hotel.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileResponse toProfileResponse(User user) {
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
