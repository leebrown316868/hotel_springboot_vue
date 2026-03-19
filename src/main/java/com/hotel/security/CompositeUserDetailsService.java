package com.hotel.security;

import com.hotel.entity.Guest;
import com.hotel.entity.User;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Composite UserDetailsService that loads users from both users table (admin/staff)
 * and guests table (customers).
 *
 * For JWT authentication, we need to load users by email from both tables.
 * This service first tries the users table (for admin/staff), then falls back to
 * the guests table (for customers).
 */
@Service
@RequiredArgsConstructor
public class CompositeUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final GuestRepository guestRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First, try to load from users table (admin/staff)
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getPassword() != null) {
                return UserDetailsImpl.build(user);
            }
        } catch (Exception e) {
            // Continue to guests table
        }

        // If not found in users table, try guests table (customers)
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if guest has a password (only registered guests can login)
        if (guest.getPassword() == null || guest.getPassword().isEmpty()) {
            throw new UsernameNotFoundException("Guest account has no password set. Please reset password.");
        }

        return new GuestDetailsImpl(guest);
    }
}
