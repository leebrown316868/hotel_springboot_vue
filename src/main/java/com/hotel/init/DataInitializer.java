package com.hotel.init;

import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.entity.RoomType;
import com.hotel.entity.User;
import com.hotel.entity.UserRole;
import com.hotel.repository.RoomRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoomRepository roomRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        logger.info("DataInitializer started");

        // Create admin account
        createAdminAccount();

        // Create staff account
        createStaffAccount();

        // Initialize room data
        initializeRooms();

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

    private void initializeRooms() {
        if (roomRepository.count() > 0) {
            logger.info("Room data already exists, skipping initialization");
            return;
        }

        logger.info("Initializing room data...");

        // 1楼（4个房间）
        createRoom("101", "1", RoomType.SINGLE, RoomStatus.AVAILABLE, new BigDecimal("85"));
        createRoom("102", "1", RoomType.DOUBLE, RoomStatus.OCCUPIED, new BigDecimal("120"));
        createRoom("103", "1", RoomType.SINGLE, RoomStatus.AVAILABLE, new BigDecimal("85"));
        createRoom("104", "1", RoomType.DOUBLE, RoomStatus.OCCUPIED, new BigDecimal("120"));

        // 2楼（4个房间）
        createRoom("201", "2", RoomType.SUITE, RoomStatus.CLEANING, new BigDecimal("250"));
        createRoom("202", "2", RoomType.DOUBLE, RoomStatus.CLEANING, new BigDecimal("120"));
        createRoom("205", "2", RoomType.DOUBLE, RoomStatus.AVAILABLE, new BigDecimal("120"));
        createRoom("206", "2", RoomType.SUITE, RoomStatus.AVAILABLE, new BigDecimal("280"));

        // 3楼（4个房间）
        createRoom("302", "3", RoomType.SUITE, RoomStatus.MAINTENANCE, new BigDecimal("300"));
        createRoom("305", "3", RoomType.SINGLE, RoomStatus.AVAILABLE, new BigDecimal("85"));
        createRoom("306", "3", RoomType.DOUBLE, RoomStatus.OCCUPIED, new BigDecimal("125"));
        createRoom("308", "3", RoomType.EXECUTIVE_SUITE, RoomStatus.AVAILABLE, new BigDecimal("450"));

        // 4楼（3个房间）
        createRoom("401", "4", RoomType.EXECUTIVE_SUITE, RoomStatus.AVAILABLE, new BigDecimal("450"));
        createRoom("402", "4", RoomType.SUITE, RoomStatus.AVAILABLE, new BigDecimal("280"));
        createRoom("501", "5", RoomType.PRESIDENTIAL_SUITE, RoomStatus.AVAILABLE, new BigDecimal("880"));

        logger.info("Room data initialized successfully: 15 rooms created");
    }

    private void createRoom(String number, String floor, RoomType type, RoomStatus status, BigDecimal price) {
        Room room = Room.builder()
                .number(number)
                .floor(floor)
                .type(type)
                .status(status)
                .price(price)
                .build();
        roomRepository.save(room);
        logger.debug("Room created: {} - {} ({})", number, type.getDisplayName(), floor + "楼");
    }
}
