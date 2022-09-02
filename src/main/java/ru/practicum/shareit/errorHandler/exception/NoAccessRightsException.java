package ru.practicum.shareit.errorHandler.exception;

public class NoAccessRightsException extends RuntimeException {

    public NoAccessRightsException(String message) {
        super(message);
    }
}