package com.backend.exception;

public class TavilyClientException extends RuntimeException {

    public TavilyClientException(String message) {
        super(message);
    }

    public TavilyClientException(String message, Throwable cause) {
        super(message, cause);
    }
}