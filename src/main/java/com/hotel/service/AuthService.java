package com.hotel.service;

import com.hotel.dto.*;
import com.hotel.entity.Guest;
import com.hotel.entity.GuestStatus;
import com.hotel.entity.UserRole;
import com.hotel.exception.AuthenticationException;
import com.hotel.repository.GuestRepository;
import com.hotel.security.GuestDetailsImpl;
import com.hotel.security.GuestDetailsService;
import com.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GuestRepository guestRepository;
    private final GuestDetailsService guestDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(AuthRequest request) {
        // 通过 GuestDetailsService 加载用户
        UserDetails userDetails = guestDetailsService.loadUserByUsername(request.getEmail());

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // 生成 token
        String token = jwtUtil.generateToken(userDetails);

        // 获取 Guest 实体用于返回
        Guest guest = guestRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Guest not found"));

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(guest))
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        // 检查邮箱是否已存在
        if (guestRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists");
        }

        // 创建新 Guest
        Guest guest = Guest.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .country(request.getCountry())
                .role(UserRole.CUSTOMER)
                .status(GuestStatus.ACTIVE)
                .totalBookings(0)
                .build();

        guest = guestRepository.save(guest);

        // 使用 GuestDetailsImpl 生成 token
        GuestDetailsImpl userDetails = new GuestDetailsImpl(guest);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(guest))
                .build();
    }

    public UserResponse getCurrentUser(String email) {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Guest not found"));

        return mapToUserResponse(guest);
    }

    public String logout() {
        return "Logged out successfully";
    }

    private UserResponse mapToUserResponse(Guest guest) {
        return UserResponse.builder()
                .id(guest.getId())
                .name(guest.getName())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .country(guest.getCountry())
                .role(guest.getRole().name())
                .build();
    }
}
