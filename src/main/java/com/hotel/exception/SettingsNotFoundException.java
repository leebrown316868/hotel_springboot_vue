package com.hotel.exception;

public class SettingsNotFoundException extends RuntimeException {
    public SettingsNotFoundException(String message) {
        super(message);
    }
}
