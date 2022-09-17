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
            throw new InvalidRequestException(String.format("при преобразовании строки произошла непредвиденная ошибка " +
                    "value=%s into BookingState", source));
        }
    }
}
