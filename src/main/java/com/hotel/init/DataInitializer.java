package com.hotel.init;

import com.hotel.entity.User;
import com.hotel.entity.UserRole;
import com.hotel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

import java.time.LocalDateTime;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        logger.info("DataInitializer started");

        // Create admin account
        createAdminAccount();

        // Create staff account
        createStaffAccount();

        logger.info("DataInitializer completed");
    }

    private void createAdminAccount() {
        String email = "admin@hotel.com";

        if (userRepository.existsByEmail(email)) {
            logger.info("Admin account already exists, skipping creation");
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setName("管理员");
        admin.setRole(UserRole.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userRepository.save(admin);
        logger.info("Admin account created successfully: {}", email);
    }

    private void createStaffAccount() {
        String email = "staff@hotel.com";

        if (userRepository.existsByEmail(email)) {
            logger.info("Staff account already exists, skipping creation");
            return;
        }

        User staff = new User();
        staff.setEmail(email);
        staff.setPassword(passwordEncoder.encode("staff123"));
        staff.setName("前台");
        staff.setRole(UserRole.STAFF);
        staff.setCreatedAt(LocalDateTime.now());
        staff.setUpdatedAt(LocalDateTime.now());

        userRepository.save(staff);
        logger.info("Staff account created successfully: {}", email);
    }
}
