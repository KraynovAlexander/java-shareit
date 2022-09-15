package ru.practicum.shareit.booking.model.mapper;

import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.dto.BookingShortDto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static BookingOutDto toBookingDto(Booking booking) {
        return BookingOutDto.builder()
                .id(booking.getId())
                .booker(booking.getBooker())
                .item(booking.getItem())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus().getStatus())
                .build();
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    public static Booking toBooking(BookingInDto bookingInDto, User user, Item item) {
        return Booking.builder()
                .id(bookingInDto.getId())
                .booker(user)
                .item(item)
                .start(bookingInDto.getStart())
                .end(bookingInDto.getEnd())
                .status(Status.valueOf(bookingInDto.getStatus()))
                .build();
    }
}
