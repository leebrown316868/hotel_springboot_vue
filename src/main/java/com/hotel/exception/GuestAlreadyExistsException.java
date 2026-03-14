package com.hotel.exception;

public class GuestAlreadyExistsException extends RuntimeException {

    public GuestAlreadyExistsException(String message) {
        super(message);
    }

    public GuestAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
