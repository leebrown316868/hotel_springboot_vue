package com.hotel.exception;

public class InvalidRoomStatusException extends RuntimeException {
    public InvalidRoomStatusException(String message) {
        super(message);
    }
}
