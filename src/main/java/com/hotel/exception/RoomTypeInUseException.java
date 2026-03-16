package com.hotel.exception;

public class RoomTypeInUseException extends RuntimeException {

    public RoomTypeInUseException(String message) {
        super(message);
    }
}
