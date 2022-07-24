package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    REJECTED("REJECTED"),
    APPROVED("APPROVED"),
    WAITING("WAITING"),
    CANCELED("CANCELED");

    private final String status;
}