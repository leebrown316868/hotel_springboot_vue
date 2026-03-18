package com.hotel.security;

import com.hotel.entity.Guest;
import com.hotel.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestDetailsService implements UserDetailsService {

    private final GuestRepository guestRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Guest not found with email: " + email));

        // 只有有密码的 Guest 才能登录（通过注册创建的用户）
        if (guest.getPassword() == null || guest.getPassword().isEmpty()) {
            throw new UsernameNotFoundException("Guest account has no password set. Please reset password.");
        }

        return new GuestDetailsImpl(guest);
    }
}
