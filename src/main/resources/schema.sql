CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    address TEXT,
    nationality VARCHAR(100),
    preferences_enabled TINYINT(1) DEFAULT 1 NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Create rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id INT PRIMARY KEY AUTO_INCREMENT,
    number VARCHAR(50) NOT NULL UNIQUE,
    floor VARCHAR(10) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    capacity INT NOT NULL,
    images TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Create guests table
CREATE TABLE IF NOT EXISTS guests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_bookings INT NOT NULL DEFAULT 0,
    last_stay DATE,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    password VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_number VARCHAR(20) UNIQUE NOT NULL,
    guest_id INT NOT NULL,
    guest_name VARCHAR(100) NOT NULL,
    room_id INT NOT NULL,
    room_type VARCHAR(20) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_count INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (guest_id) REFERENCES guests (id),
    FOREIGN KEY (room_id) REFERENCES rooms (id),
    CHECK (
        check_out_date > check_in_date
    ),
    CHECK (guest_count > 0),
    CHECK (total_amount >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Create indexes for bookings table
CREATE INDEX idx_booking_number ON bookings (booking_number);

CREATE INDEX idx_booking_guest ON bookings (guest_id);

CREATE INDEX idx_booking_room ON bookings (room_id);

CREATE INDEX idx_booking_status ON bookings (status);

CREATE INDEX idx_booking_dates ON bookings (check_in_date, check_out_date);

-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    user_id INT,
    guest_id INT,
    rating INT NOT NULL CHECK (
        rating >= 1
        AND rating <= 5
    ),
    comment TEXT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (guest_id) REFERENCES guests (id) ON DELETE CASCADE,
    UNIQUE (booking_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Create indexes for reviews table
CREATE INDEX idx_reviews_user ON reviews (user_id);

CREATE INDEX idx_reviews_booking ON reviews (booking_id);

-- Create system_settings table
CREATE TABLE IF NOT EXISTS system_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_name VARCHAR(100) NOT NULL DEFAULT 'GrandHorizon 酒店及水疗中心',
    contact_email VARCHAR(100) NOT NULL DEFAULT 'contact@grandhorizon.com',
    contact_phone VARCHAR(20) NOT NULL DEFAULT '+1 (555) 123-4567',
    address VARCHAR(200),
    description VARCHAR(1000) DEFAULT '欢迎来到 GrandHotel 豪华酒店，我们致力于为您提供最舒适的住宿体验。',
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY',
    timezone VARCHAR(10) NOT NULL DEFAULT 'UTC+8',
    language VARCHAR(20) NOT NULL DEFAULT 'Chinese',
    two_factor_enabled TINYINT(1) NOT NULL DEFAULT 0,
    session_timeout INT NOT NULL DEFAULT 30,
    password_expiry INT NOT NULL DEFAULT 90,
    email_notification_bookings TINYINT(1) NOT NULL DEFAULT 1,
    email_notification_cancellations TINYINT(1) NOT NULL DEFAULT 1,
    push_notifications_enabled TINYINT(1) NOT NULL DEFAULT 1,
    room_types_config TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    priority INT NOT NULL DEFAULT 0,
    action_link VARCHAR(200),
    created_at DATETIME NOT NULL,
    read_at DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Create indexes for notifications table
CREATE INDEX idx_user_unread ON notifications (user_id, is_read);

CREATE INDEX idx_user_created ON notifications (user_id, created_at);

-- Create room_types table
CREATE TABLE IF NOT EXISTS room_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Create indexes for room_types table
CREATE INDEX idx_room_types_code ON room_types (code);

CREATE INDEX idx_room_types_active ON room_types (active);