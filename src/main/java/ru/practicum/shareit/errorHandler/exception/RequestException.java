package ru.practicum.shareit.errorHandler.exception;

public class RequestException extends RuntimeException {

    public RequestException(String message) {
        super(message);
    }
}