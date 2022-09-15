package ru.practicum.shareit.errorHandler.exceptions;

public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(String message) {
        super(message);
    }
}
