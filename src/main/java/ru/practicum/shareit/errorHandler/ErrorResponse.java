package ru.practicum.shareit.errorHandler;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    private final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    @JsonProperty("Сообщение об ошибке")
    public String getMessage() {
        return message;
    }
}