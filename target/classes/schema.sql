-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    phone VARCHAR,
    address TEXT,
    nationality VARCHAR,
    preferences_enabled INTEGER DEFAULT 1 NOT NULL,
    role VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    number VARCHAR NOT NULL UNIQUE,
    floor VARCHAR NOT NULL,
    type VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    price DECIMAL NOT NULL,
    capacity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create guests table
CREATE TABLE IF NOT EXISTS guests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_bookings INTEGER NOT NULL DEFAULT 0,
    last_stay DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_number VARCHAR(20) UNIQUE NOT NULL,
    guest_id INTEGER NOT NULL,
    guest_name VARCHAR(100) NOT NULL,
    room_id INTEGER NOT NULL,
    room_type VARCHAR(20) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    CHECK (check_out_date > check_in_date),
    CHECK (guest_count > 0),
    CHECK (total_amount >= 0)
);

-- Create indexes for bookings table
CREATE INDEX IF NOT EXISTS idx_booking_number ON bookings(booking_number);
CREATE INDEX IF NOT EXISTS idx_booking_guest ON bookings(guest_id);
CREATE INDEX IF NOT EXISTS idx_booking_room ON bookings(room_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_booking_dates ON bookings(check_in_date, check_out_date);

-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(booking_id)
);

-- Create indexes for reviews table
CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_booking ON reviews(booking_id);

-- Create system_settings table
CREATE TABLE IF NOT EXISTS system_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    hotel_name VARCHAR(100) NOT NULL DEFAULT 'GrandHorizon 酒店及水疗中心',
    contact_email VARCHAR(100) NOT NULL DEFAULT 'contact@grandhorizon.com',
    contact_phone VARCHAR(20) NOT NULL DEFAULT '+1 (555) 123-4567',
    address VARCHAR(200),
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY',
    timezone VARCHAR(10) NOT NULL DEFAULT 'UTC+8',
    language VARCHAR(20) NOT NULL DEFAULT 'Chinese',
    two_factor_enabled INTEGER NOT NULL DEFAULT 0,
    session_timeout INTEGER NOT NULL DEFAULT 30,
    password_expiry INTEGER NOT NULL DEFAULT 90,
    email_notification_bookings INTEGER NOT NULL DEFAULT 1,
    email_notification_cancellations INTEGER NOT NULL DEFAULT 1,
    push_notifications_enabled INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system'
);

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read INTEGER NOT NULL DEFAULT 0,
    priority INTEGER NOT NULL DEFAULT 0,
    action_link VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP
);

-- Create indexes for notifications table
CREATE INDEX IF NOT EXISTS idx_user_unread ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_user_created ON notifications(user_id, created_at);
