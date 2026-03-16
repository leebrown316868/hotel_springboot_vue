package com.hotel.exception;

public class RoomTypeAlreadyExistsException extends RuntimeException {

    public RoomTypeAlreadyExistsException(String message) {
        super(message);
    }
}
