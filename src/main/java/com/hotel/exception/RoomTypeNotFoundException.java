package com.hotel.exception;

public class RoomTypeNotFoundException extends RuntimeException {

    public RoomTypeNotFoundException(Long id) {
        super("房型不存在: " + id);
    }

    public RoomTypeNotFoundException(String message) {
        super(message);
    }
}
