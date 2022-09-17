package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.mapper.BookingMapper;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.errorHandler.exceptions.BookingNotFoundException;
import ru.practicum.shareit.errorHandler.exceptions.InvalidRequestException;
import ru.practicum.shareit.errorHandler.exceptions.ItemNotFoundException;
import ru.practicum.shareit.errorHandler.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.utils.Pagination;

import java.time.LocalDateTime;
import java.util.List;
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
    public BookingOutDto addNewBooking(long userId, BookingInDto bookingInDto) {
        Item item = itemRepository.findById(bookingInDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(String.format("Item with id=%s not found",
                        bookingInDto.getItemId())));
        if (!item.getAvailable())
            throw new InvalidRequestException("the booking isn't possible because the item isn't available");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", userId)));
        if (userId == item.getOwner().getId())
            throw new ItemNotFoundException("the user trying to book his own item");

        bookingInDto.setStatus(Status.WAITING.getStatus());

        return BookingMapper.toBookingDto(bookingRepository.save(BookingMapper.toBooking(bookingInDto, user, item)));
    }

    @Override
    public BookingOutDto updateStatus(long userId, long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(String.format("Booking with id=%s not found", bookingId)));

        if (userId != booking.getItem().getOwner().getId())
            throw new BookingNotFoundException(String.format("booking with id=%s for the user with id=%s was not found",
                    bookingId, userId));
        if (booking.getStatus() == Status.APPROVED || booking.getStatus() == Status.REJECTED)
            throw new InvalidRequestException("the status cannot be changed");

        booking.setStatus(isApproved ? Status.APPROVED : Status.REJECTED);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto getById(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(String.format("Booking with id=%s not found", bookingId)));

        if (userId != booking.getItem().getOwner().getId() && userId != booking.getBooker().getId())
            throw new BookingNotFoundException(String.format("booking with id=%s for the user with id=%s was not found",
                    bookingId, userId));

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingOutDto> getUserBookings(long userId, BookingState state, int from, int size) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(String.format("User with id=%s not found", userId));

        Pageable sortedByStartDesc = Pagination.of(from, size, Sort.by("start").descending());

        return getFilteredBookingsByStateAndBookerId(userId, sortedByStartDesc, state).get()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingOutDto> getBookingsByOwnerId(long ownerId, BookingState state, int from, int size) {
        if (!itemRepository.existsByOwnerId(ownerId))
            throw new UserNotFoundException(String.format("User with id=%s is not the owner of any thing", ownerId));

        Pageable sortedByStartDesc = Pagination.of(from, size, Sort.by("start").descending());

        return getFilteredBookingsByStateAndOwnerId(ownerId, sortedByStartDesc, state).get()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private Slice<Booking> getFilteredBookingsByStateAndBookerId(long bookerId, Pageable pageable, BookingState state) {
        switch (state) {
            case ALL:
                return bookingRepository.getAllByBookerId(bookerId, pageable);
            case CURRENT:
                return bookingRepository.getAllCurrentByBookerId(bookerId, LocalDateTime.now(), pageable);
            case FUTURE:
                return bookingRepository.getAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), pageable);
            case PAST:
                return bookingRepository.getAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), pageable);
            default:
                return bookingRepository.getAllByBookerIdAndStatus(bookerId, Status.valueOf(state.name()), pageable);
        }
    }

    private Slice<Booking> getFilteredBookingsByStateAndOwnerId(long ownerId, Pageable pageable, BookingState state) {
        switch (state) {
            case ALL:
                return bookingRepository.getAllByOwnerId(ownerId, pageable);
            case CURRENT:
                return bookingRepository.getAllCurrentByOwnerId(ownerId, LocalDateTime.now(), pageable);
            case FUTURE:
                return bookingRepository.getAllFutureByOwnerId(ownerId, LocalDateTime.now(), pageable);
            case PAST:
                return bookingRepository.getAllPastByOwnerId(ownerId, LocalDateTime.now(), pageable);
            default:
                return bookingRepository.getAllByOwnerIdAndStatus(ownerId, Status.valueOf(state.name()), pageable);
        }
    }
}
