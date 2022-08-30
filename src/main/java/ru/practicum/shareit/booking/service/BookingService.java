package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;

import java.util.List;

public interface BookingService {

    BookingOutDto addNewBooking(long userId, BookingInDto bookingInDto);

    BookingOutDto updateStatus(long userId, long bookingId, boolean isApproved);

    BookingOutDto getById(long userId, long bookingId);

    List<BookingOutDto> getUserBookings(long userId, String state, int from, int size);

    List<BookingOutDto> getBookingsByOwnerId(long ownerId, String state, int from, int size);
}
