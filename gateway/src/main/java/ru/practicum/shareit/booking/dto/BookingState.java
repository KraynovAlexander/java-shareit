package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum BookingState {

    ALL("ALL"),
    CURRENT("CURRENT"),
    PAST("PAST"),
    FUTURE("FUTURE"),
    WAITING("WAITING"),
    REJECTED("REJECTED");

    private final String name;

    public static boolean isBookingState(String stringState) {
        return Arrays.stream(BookingState.values())
                .anyMatch(bookingState -> bookingState.name.equalsIgnoreCase(stringState));
    }
}