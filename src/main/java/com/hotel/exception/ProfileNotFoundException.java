package com.hotel.exception;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String message) {
        super(message);
    }

    public ProfileNotFoundException(Long userId) {
        super("用户资料不存在: " + userId);
    }
}
