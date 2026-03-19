package com.hotel.service;

import com.hotel.dto.ChangePasswordRequest;
import com.hotel.dto.ProfileResponse;
import com.hotel.dto.UpdateProfileRequest;
import com.hotel.entity.Guest;
import com.hotel.entity.User;
import com.hotel.exception.ProfileNotFoundException;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(String email) {
        // 先尝试从users表查找（管理员/员工）
        return userRepository.findByEmail(email)
                .map(this::mapUserToProfileResponse)
                .orElseGet(() -> {
                    // 如果没找到，从guests表查找（客户）
                    return guestRepository.findByEmail(email)
                            .map(this::mapGuestToProfileResponse)
                            .orElseThrow(() -> new ProfileNotFoundException("用户不存在"));
                });
    }

    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        // 先尝试从users表查找
        return userRepository.findByEmail(email)
                .map(user -> updateUserProfile(user, request))
                .orElseGet(() -> {
                    // 如果没找到，从guests表查找
                    return guestRepository.findByEmail(email)
                            .map(guest -> updateGuestProfile(guest, request))
                            .orElseThrow(() -> new ProfileNotFoundException("用户不存在"));
                });
    }

    private ProfileResponse updateUserProfile(User user, UpdateProfileRequest request) {
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setNationality(request.getNationality());
        user.setPreferencesEnabled(request.getPreferencesEnabled());
        user = userRepository.save(user);
        return mapUserToProfileResponse(user);
    }

    private ProfileResponse updateGuestProfile(Guest guest, UpdateProfileRequest request) {
        guest.setName(request.getName());
        guest.setPhone(request.getPhone());
        guest.setCountry(request.getNationality());
        guest = guestRepository.save(guest);
        return mapGuestToProfileResponse(guest);
    }

    private ProfileResponse mapUserToProfileResponse(User user) {
        String[] nameParts = user.getName().split(" ", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .name(user.getName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .nationality(user.getNationality())
                .preferencesEnabled(user.getPreferencesEnabled())
                .role(user.getRole().name())
                .build();
    }

    private ProfileResponse mapGuestToProfileResponse(Guest guest) {
        return ProfileResponse.builder()
                .id(guest.getId())
                .email(guest.getEmail())
                .name(guest.getName())
                .firstName(guest.getName())
                .lastName("")
                .phone(guest.getPhone())
                .address("")
                .nationality(guest.getCountry())
                .preferencesEnabled(true)
                .role(guest.getRole().name())
                .build();
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        // 先尝试从users表查找
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            changeUserPassword(user, request);
            return;
        }

        // 如果没找到，从guests表查找
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException("用户不存在"));
        changeGuestPassword(guest, request);
    }

    private void changeUserPassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private void changeGuestPassword(Guest guest, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), guest.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }
        guest.setPassword(passwordEncoder.encode(request.getNewPassword()));
        guestRepository.save(guest);
    }
}
