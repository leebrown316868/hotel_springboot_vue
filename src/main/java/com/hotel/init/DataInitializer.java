package com.hotel.init;

import com.hotel.entity.Guest;
import com.hotel.entity.GuestStatus;
import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.entity.RoomType;
import com.hotel.entity.RoomTypeEntity;
import com.hotel.entity.SystemSettings;
import com.hotel.entity.User;
import com.hotel.entity.UserRole;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.repository.SystemSettingsRepository;
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
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
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

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private SystemSettingsRepository systemSettingsRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        logger.info("DataInitializer started");

        // Migrate database schema if needed
        migrateDatabase();

        // Create admin account
        createAdminAccount();

        // Create staff account
        createStaffAccount();

        // Initialize room types
        initializeRoomTypes();

        // Initialize room data
        initializeRooms();

        // Initialize guest data
        initializeGuests();

        // Initialize system settings
        initializeSystemSettings();

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

    private void initializeRoomTypes() {
        if (roomTypeRepository.count() > 0) {
            logger.info("Room type data already exists, skipping initialization");
            return;
        }

        logger.info("Initializing room type data...");

        createRoomType("SINGLE", "单人间", 1, new BigDecimal("85"));
        createRoomType("DOUBLE", "双人间", 2, new BigDecimal("120"));
        createRoomType("SUITE", "套房", 4, new BigDecimal("250"));
        createRoomType("EXECUTIVE_SUITE", "行政套房", 2, new BigDecimal("450"));
        createRoomType("PRESIDENTIAL_SUITE", "总统套房", 2, new BigDecimal("880"));

        logger.info("Room type data initialized successfully: 5 room types created");
    }

    private void createRoomType(String code, String name, int capacity, BigDecimal basePrice) {
        if (roomTypeRepository.existsByCode(code)) {
            logger.debug("Room type already exists: {}", code);
            return;
        }

        RoomTypeEntity roomType = RoomTypeEntity.builder()
                .code(code)
                .name(name)
                .capacity(capacity)
                .basePrice(basePrice)
                .active(true)
                .build();
        roomTypeRepository.save(roomType);
        logger.debug("Room type created: {} - {}", code, name);
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
        // 根据房型设置容量
        Integer capacity;
        switch (type) {
            case SINGLE:
                capacity = 1;
                break;
            case DOUBLE:
                capacity = 2;
                break;
            case SUITE:
                capacity = 4;
                break;
            case EXECUTIVE_SUITE:
                capacity = 2;
                break;
            case PRESIDENTIAL_SUITE:
                capacity = 2;
                break;
            default:
                capacity = 2;
        }

        Room room = new Room();
        room.setNumber(number);
        room.setFloor(floor);
        room.setType(type);
        room.setStatus(status);
        room.setPrice(price);
        room.setCapacity(capacity);
        roomRepository.save(room);
        logger.debug("Room created: {} - {} ({})", number, type.getDisplayName(), floor + "楼");
    }

    private void initializeGuests() {
        // 不再自动创建mock客户数据
        logger.info("Skipping guest data initialization - mock data disabled");
    }

    private void createGuest(String name, String email, String phone, String country,
                             GuestStatus status, int totalBookings, LocalDate lastStay) {
        Guest guest = Guest.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .country(country)
                .status(status)
                .totalBookings(totalBookings)
                .lastStay(lastStay)
                .build();
        guestRepository.save(guest);
        logger.debug("Guest created: {} ({}) - {} bookings, status: {}", name, country, totalBookings, status);
    }

    private void migrateDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            // Check if capacity column exists in rooms table
            ResultSet rs = conn.getMetaData().getColumns(null, null, "rooms", "capacity");
            if (!rs.next()) {
                logger.info("Adding capacity column to rooms table...");
                conn.createStatement().executeUpdate("ALTER TABLE rooms ADD COLUMN capacity INTEGER DEFAULT 2 NOT NULL");
                logger.info("Capacity column added successfully");
            } else {
                logger.debug("Capacity column already exists in rooms table");
            }
            rs.close();

            // Check if room_types_config column exists in system_settings table
            ResultSet rs2 = conn.getMetaData().getColumns(null, null, "system_settings", "room_types_config");
            if (!rs2.next()) {
                logger.info("Adding room_types_config column to system_settings table...");
                conn.createStatement().executeUpdate("ALTER TABLE system_settings ADD COLUMN room_types_config TEXT");
                logger.info("room_types_config column added successfully");
            } else {
                logger.debug("room_types_config column already exists in system_settings table");
            }
            rs2.close();

            // Check if room_types table exists
            ResultSet rs3 = conn.getMetaData().getTables(null, null, "room_types", null);
            if (!rs3.next()) {
                logger.info("Creating room_types table...");
                conn.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS room_types (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code VARCHAR NOT NULL UNIQUE, " +
                    "name VARCHAR NOT NULL, " +
                    "capacity INTEGER NOT NULL, " +
                    "base_price DECIMAL NOT NULL, " +
                    "active INTEGER NOT NULL DEFAULT 1, " +
                    "created_at TIMESTAMP NOT NULL, " +
                    "updated_at TIMESTAMP NOT NULL)"
                );
                conn.createStatement().executeUpdate("CREATE INDEX IF NOT EXISTS idx_room_types_code ON room_types(code)");
                conn.createStatement().executeUpdate("CREATE INDEX IF NOT EXISTS idx_room_types_active ON room_types(active)");
                logger.info("room_types table created successfully");
            } else {
                logger.debug("room_types table already exists");
            }
            rs3.close();

            // Check if images column exists in rooms table
            ResultSet rs4 = conn.getMetaData().getColumns(null, null, "rooms", "images");
            if (!rs4.next()) {
                logger.info("Adding images column to rooms table...");
                conn.createStatement().executeUpdate("ALTER TABLE rooms ADD COLUMN images TEXT");
                logger.info("Images column added successfully");
            } else {
                logger.debug("Images column already exists in rooms table");
            }
            rs4.close();
        } catch (Exception e) {
            logger.warn("Database migration failed (may already exist): {}", e.getMessage());
        }
    }

    private void initializeSystemSettings() {
        try {
            if (systemSettingsRepository.count() > 0) {
                logger.info("System settings already exist, skipping initialization");
                return;
            }

            SystemSettings settings = new SystemSettings();
            systemSettingsRepository.save(settings);
            logger.info("Default system settings initialized successfully");
        } catch (Exception e) {
            logger.warn("System settings initialization failed: {}", e.getMessage());
        }
    }
}
