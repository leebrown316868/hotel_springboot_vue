package com.hotel.exception;

public class ReviewAlreadyExistsException extends RuntimeException {

    public ReviewAlreadyExistsException(String message) {
        super(message);
    }

    public ReviewAlreadyExistsException(Long bookingId) {
        super("该订单已评价: " + bookingId);
    }
}
