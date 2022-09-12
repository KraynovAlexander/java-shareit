package ru.practicum.shareit.booking.enums.enumConverter;

import org.springframework.core.convert.converter.Converter;

import org.springframework.stereotype.Component;

import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.errorHandler.exceptions.InvalidRequestException;

@Component
public class StringToBookingStateConverter implements Converter<String, BookingState> {

    @Override
    public BookingState convert(String source) {
        try {
            return BookingState.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(String.format("an unexpected error occurred when converting string " +
                    "value=%s into BookingState", source));
        }
    }
}
