package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.errorHandler.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.utils.Pagination;

import java.time.LocalDateTime;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public List<BookingOutDto> getUserBookings(long userId, String state, int from, int size) {
        checkValidState(state);
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(String.format("Пользователь с id=%s не найден", userId));

        Pageable sortedByStartDesc = Pagination.of(from, size, Sort.by("start").descending());

        return getFilteredBookingsByStateAndBookerId(userId, sortedByStartDesc, state).get()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingOutDto> getBookingsByOwnerId(long ownerId, String state, int from, int size) {
        checkValidState(state);
        if (!itemRepository.existsByOwnerId(ownerId))
            throw new UserNotFoundException(String.format("Пользователь с id=%s не является владельцем чего-либо", ownerId));

        Pageable sortedByStartDesc = Pagination.of(from, size, Sort.by("start").descending());

        return getFilteredBookingsByStateAndOwnerId(ownerId, sortedByStartDesc, State.valueOf(state)).get()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingOutDto addNewBooking(long userId, BookingInDto bookingInDto) {
        Item item = itemRepository.findById(bookingInDto.getItemId())
                .orElseThrow(() -> new ItemException(String.format("Предмет с id=%s не найден",
                        bookingInDto.getItemId())));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        LocalDateTime start = bookingInDto.getStart();
        LocalDateTime end = bookingInDto.getEnd();
        if (userId == item.getOwner().getId()) throw new ItemException("пользователь пытается забронировать свой собственный товар");
        if (!item.getAvailable() || start.isBefore(LocalDateTime.now()) || start.isAfter(end))
            throw new RequestException("попытка бронирования не удалась из-за неверных данных");

        bookingInDto.setStatus(Status.WAITING.getStatus());
        return BookingMapper.toBookingDto(bookingRepository.save(BookingMapper.toBooking(bookingInDto, user, item)));
    }

    @Override
    public BookingOutDto updateStatus(long userId, long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(String.format("Бронирование с помощью id=%s не возможно", bookingId)));

        if (userId != booking.getItem().getOwner().getId())
            throw new BookingException(String.format("бронирование с помощью id=%s для пользователя с id=%s не может быть",
                    bookingId, userId));
        if (booking.getStatus() == Status.APPROVED || booking.getStatus() == Status.REJECTED)
            throw new RequestException("статус не может быть изменен");

        booking.setStatus(isApproved ? Status.APPROVED : Status.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto getById(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(String.format("Бронирование с помощью id=%s не найдено", bookingId)));

        if (userId != booking.getItem().getOwner().getId() && userId != booking.getBooker().getId())
            throw new BookingException(String.format("бронирование с помощью id=%s для пользователя с id=%s не может быть",
                    bookingId, userId));

        return BookingMapper.toBookingDto(booking);
    }


    private Slice<Booking> getFilteredBookingsByStateAndBookerId(long bookerId, Pageable pageable, String state) {
        switch (state) {
            case "ALL":
                return bookingRepository.getAllByBookerId(bookerId, pageable);
            case "CURRENT":
                return bookingRepository.getAllCurrentByBookerId(bookerId, LocalDateTime.now(), pageable);
            case "FUTURE":
                return bookingRepository.getAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), pageable);
            case "PAST":
                return bookingRepository.getAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), pageable);
            default:
                return bookingRepository.getAllByBookerIdAndStatus(bookerId, Status.valueOf(state), pageable);
        }
    }

    private Slice<Booking> getFilteredBookingsByStateAndOwnerId(long ownerId, Pageable pageable, State state) {
        switch (state.getState()) {
            case "ALL":
                return bookingRepository.getAllByOwnerId(ownerId, pageable);
            case "CURRENT":
                return bookingRepository.getAllCurrentByOwnerId(ownerId, LocalDateTime.now(), pageable);
            case "FUTURE":
                return bookingRepository.getAllFutureByOwnerId(ownerId, LocalDateTime.now(), pageable);
            case "PAST":
                return bookingRepository.getAllPastByOwnerId(ownerId, LocalDateTime.now(), pageable);
            default:
                return bookingRepository.getAllByOwnerIdAndStatus(ownerId, Status.valueOf(state.getState()), pageable);
        }
    }

    private void checkValidState(String state) {
        if (Arrays.stream(State.values()).noneMatch(element -> Objects.equals(element.getState(), state)))
            throw new RequestException("Unknown state: UNSUPPORTED_STATUS");
    }
}
