package com.hotel.service;

import com.hotel.dto.*;
import com.hotel.entity.Guest;
import com.hotel.entity.GuestStatus;
import com.hotel.entity.User;
import com.hotel.entity.UserRole;
import com.hotel.exception.AuthenticationException;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.UserRepository;
import com.hotel.security.GuestDetailsImpl;
import com.hotel.security.UserDetailsImpl;
import com.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GuestRepository guestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // 先尝试从 users 表加载（管理员和员工）
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getPassword() != null) {
                // 验证密码
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new AuthenticationException("Invalid email or password");
                }

                // 生成 token
                UserDetails userDetails = UserDetailsImpl.build(user);
                String token = jwtUtil.generateToken(userDetails);

                return AuthResponse.builder()
                        .token(token)
                        .user(mapToUserResponse(user))
                        .build();
            }
        } catch (Exception e) {
            // 继续尝试 guests 表
        }

        // 尝试从 guests 表加载（客户）
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // 检查 guest 是否有密码（只有注册用户才有密码）
        if (guest.getPassword() == null || guest.getPassword().isEmpty()) {
            throw new AuthenticationException("This account cannot login directly. Please use social login or contact support.");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, guest.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // 生成 token
        UserDetails userDetails = new GuestDetailsImpl(guest);
        String token = jwtUtil.generateToken(userDetails);

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
        // 先尝试从 users 表加载
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return mapToUserResponse(user);
        }

        // 尝试从 guests 表加载
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return mapToUserResponse(guest);
    }

    public String logout() {
        return "Logged out successfully";
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .country(user.getNationality())
                .role(user.getRole().name())
                .build();
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
