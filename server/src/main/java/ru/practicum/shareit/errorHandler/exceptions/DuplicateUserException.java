package ru.practicum.shareit.errorHandler.exceptions;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
