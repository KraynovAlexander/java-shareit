package ru.practicum.shareit.errorHandler.exceptions;

public class IllegalPaginationArgumentException extends RuntimeException {

    public IllegalPaginationArgumentException(String message) {
        super(message);
    }
}
