package com.hotel.init;

import com.hotel.entity.Guest;
import com.hotel.entity.GuestStatus;
import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.entity.RoomType;
import com.hotel.entity.SystemSettings;
import com.hotel.entity.User;
import com.hotel.entity.UserRole;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.RoomRepository;
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

        Room room = Room.builder()
                .number(number)
                .floor(floor)
                .type(type)
                .status(status)
                .price(price)
                .capacity(capacity)
                .build();
        roomRepository.save(room);
        logger.debug("Room created: {} - {} ({})", number, type.getDisplayName(), floor + "楼");
    }

    private void initializeGuests() {
        try {
            if (guestRepository.count() > 0) {
                logger.info("Guest data already exists, skipping initialization");
                return;
            }
        } catch (Exception e) {
            logger.info("Guests table not found or error checking count, will attempt to create data: {}", e.getMessage());
            // 表可能还不存在，继续尝试创建数据
        }

        logger.info("Initializing guest data...");

        // 中国客户
        createGuest("张三", "zhangsan@example.com", "13800138000", "中国", GuestStatus.VIP, 12, LocalDate.of(2023, 11, 1));
        createGuest("李四", "lisi@example.com", "13900139000", "中国", GuestStatus.ACTIVE, 5, LocalDate.of(2023, 10, 24));
        createGuest("王五", "wangwu@example.com", "13700137000", "中国", GuestStatus.ACTIVE, 2, LocalDate.of(2023, 10, 25));
        createGuest("陈伟", "chen.wei@example.cn", "13500135000", "中国", GuestStatus.VIP, 15, LocalDate.of(2023, 11, 5));
        createGuest("赵敏", "zhaomin@example.cn", "13600136000", "中国", GuestStatus.INACTIVE, 1, LocalDate.of(2023, 5, 10));

        // 日本客户
        createGuest("田中裕子", "y.tanaka@example.jp", "+81 3 1234 5678", "日本", GuestStatus.ACTIVE, 3, LocalDate.of(2023, 9, 15));
        createGuest("山田太郎", "t.yamada@example.jp", "+81 90 1234 5678", "日本", GuestStatus.ACTIVE, 4, LocalDate.of(2023, 10, 20));

        // 韩国客户
        createGuest("金智秀", "j.kim@example.kr", "+82 10 1234 5678", "韩国", GuestStatus.ACTIVE, 2, LocalDate.of(2023, 10, 18));

        // 德国客户
        createGuest("汉斯·穆勒", "h.mueller@example.de", "+49 30 1234567", "德国", GuestStatus.ACTIVE, 4, LocalDate.of(2023, 8, 20));
        createGuest("安娜·施密特", "a.schmidt@example.de", "+49 171 1234567", "德国", GuestStatus.INACTIVE, 1, LocalDate.of(2023, 6, 15));

        // 法国客户
        createGuest("苏菲·马丁", "s.martin@example.fr", "+33 1 23 45 67 89", "法国", GuestStatus.INACTIVE, 2, LocalDate.of(2023, 10, 10));
        createGuest("皮埃尔·杜邦", "p.dupont@example.fr", "+33 6 12 34 56 78", "法国", GuestStatus.ACTIVE, 3, LocalDate.of(2023, 10, 28));

        // 美国客户
        createGuest("约翰·史密斯", "j.smith@example.com", "+1 212 555 1234", "美国", GuestStatus.VIP, 8, LocalDate.of(2023, 11, 2));
        createGuest("艾米·约翰逊", "a.johnson@example.com", "+1 310 555 9876", "美国", GuestStatus.ACTIVE, 6, LocalDate.of(2023, 10, 30));

        // 英国客户
        createGuest("詹姆斯·威尔逊", "j.wilson@example.co.uk", "+44 20 7946 0958", "英国", GuestStatus.ACTIVE, 3, LocalDate.of(2023, 10, 22));

        // 澳大利亚客户
        createGuest("艾玛·布朗", "e.brown@example.com.au", "+61 2 9876 5432", "澳大利亚", GuestStatus.ACTIVE, 2, LocalDate.of(2023, 10, 25));

        logger.info("Guest data initialized successfully: 15 guests created");
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
