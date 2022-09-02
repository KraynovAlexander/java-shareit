package ru.practicum.shareit.booking.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum State {
    ALL("ALL"),
    CURRENT("CURRENT"),
    PAST("PAST"),
    FUTURE("FUTURE"),
    WAITING("WAITING"),
    REJECTED("REJECTED");

    private final String state;
}
