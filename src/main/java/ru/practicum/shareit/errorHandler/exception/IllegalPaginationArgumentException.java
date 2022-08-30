package ru.practicum.shareit.errorHandler.exception;

public class IllegalPaginationArgumentException extends RuntimeException {

    public IllegalPaginationArgumentException(String message) {
        super(message);
    }
}