package com.yf.bookstore.exception;

public abstract class BaseException extends RuntimeException {

    protected final int statusCode;

    public BaseException(String message) {
        super(message);
        this.statusCode = 503;
    }

    public BaseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

