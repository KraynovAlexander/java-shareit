package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;

import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.mapper.BookingMapper;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.errorHandler.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.utils.Pagination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private Booking booking;
    private BookingInDto bookingInDto;
    private Item item;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@ya.ru")
                .build();

        Request request = Request.builder()
                .id(1L)
                .user(user)
                .description("wanted")
                .build();

        item = Item.builder()
                .available(true)
                .request(request)
                .owner(user)
                .build();

        booking = Booking.builder()
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();

        bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING.getStatus())
                .bookerId(1L)
                .itemId(1L)
                .build();
    }

    @Test
    void whenTryToAddNewBookingOfNotExistsItemThenItemNotFoundException() {
        Mockito.when(itemRepository.findById(bookingInDto.getItemId()))
                .thenThrow(new ItemNotFoundException("Item with id=1 not found"));

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.addNewBooking(1L, bookingInDto));

        Assertions.assertEquals("Item with id=1 not found", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryToAddNewBookingByNotExistsUserThenUserNotFoundException() {
        Mockito.when(itemRepository.findById(bookingInDto.getItemId()))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(1L))
                .thenThrow(new UserNotFoundException("User with id=1 not found"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.addNewBooking(1L, bookingInDto));

        Assertions.assertEquals("User with id=1 not found", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryToBookOwnItemThenItemNotFoundException() {
        Mockito.when(itemRepository.findById(bookingInDto.getItemId()))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.addNewBooking(1L, bookingInDto));

        Assertions.assertEquals("the user trying to book his own item", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryToBookNotAvailableItemThenInvalidRequestException() {
        Item notAvailable = Item.builder()
                .id(5L)
                .owner(user)
                .available(false)
                .build();

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(5L)
                .build();

        Mockito.when(itemRepository.findById(5L))
                .thenReturn(Optional.of(notAvailable));

        final InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> bookingService.addNewBooking(2L, bookingInDto));

        Assertions.assertEquals("the booking isn't possible because the item isn't available",
                exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(5L);
        Mockito.verify(userRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenAddValidBookingThenCallSaveBookingRepository() {
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(booking);

        BookingOutDto returned = bookingService.addNewBooking(2L, bookingInDto);

        assertThat(returned, equalTo(BookingMapper.toBookingDto(booking)));

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    void whenTryToUpdateStatusOfNotExistsBookingThenBookingNotFoundException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenThrow(new BookingNotFoundException("Booking with id=1 not found"));

        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        Assertions.assertEquals("Booking with id=1 not found", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void whenTryToUpdateStatusSomeoneElseBookingThenBookingNotFoundException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.updateStatus(10L, 1L, true));

        Assertions.assertEquals("booking with id=1 for the user with id=10 was not found", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void whenTryToUpdateStatusWhenStatusAlreadyApprovedThenInvalidRequestException() {
        Booking alreadyApproved = Booking.builder()
                .id(1L)
                .item(item)
                .status(Status.APPROVED)
                .build();

        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(alreadyApproved));

        final InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        Assertions.assertEquals("the status cannot be changed", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void whenTryToUpdateStatusWhenStatusRejectedThenInvalidRequestException() {
        Booking alreadyApproved = Booking.builder()
                .id(1L)
                .item(item)
                .status(Status.REJECTED)
                .build();

        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(alreadyApproved));

        final InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        Assertions.assertEquals("the status cannot be changed", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void whenUpdateStatusValidBookingThenCallSaveBookingRepository() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(booking);

        bookingService.updateStatus(1L, 1L, true);

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(Mockito.any(Booking.class));
    }

    @Test
    void whenTryToGetByIdNotExistsBookingThenBookingNotFoundException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenThrow(new BookingNotFoundException("Booking with id=1 not found"));

        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getById(1L, 1L));

        Assertions.assertEquals("Booking with id=1 not found", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    void whenTryToGetByIdByNotOwnerThenBookingNotFoundException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getById(10L, 1L));

        Assertions.assertEquals("booking with id=1 for the user with id=10 was not found", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    void whenGetValidBookingByIdThenReturnBookingOutDto() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        BookingOutDto returned = bookingService.getById(1L, 1L);

        assertThat(returned, equalTo(BookingMapper.toBookingDto(booking)));

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    void whenTryToGetUserBookingsByNotExistsUserThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getUserBookings(1L, BookingState.ALL, 0, 10));

        Assertions.assertEquals("User with id=1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
    }

    @Test
    void whenGetUserBookingsAndStateAllThenCallGetAllByBookerIdBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByBookerId(1L, pageable))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, BookingState.ALL, 0, 10);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByBookerId(1L, pageable);

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetUserBookingsAndStateCurrentThenCallGetAllCurrentByBookerIdBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllCurrentByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, BookingState.CURRENT, 0, 10);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllCurrentByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetUserBookingsAndStateFutureThenCallGetAllByBookerIdAndStartAfterBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, BookingState.FUTURE, 0, 10);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetUserBookingsAndStatePastThenCallGetAllByBookerIdAndStartAfterBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, BookingState.PAST, 0, 10);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetUserBookingsAndStateWaitingThenCallGetAllByBookerIdAndStartAfterBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByBookerIdAndStatus(1L, Status.WAITING, pageable))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, BookingState.WAITING, 0, 10);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByBookerIdAndStatus(1L, Status.WAITING, pageable);

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
    }

    @Test
    void whenGetUserBookingsAndStateRejectedThenCallGetAllByBookerIdAndStartAfterBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByBookerIdAndStatus(1L, Status.REJECTED, pageable))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, BookingState.REJECTED, 0, 10);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByBookerIdAndStatus(1L, Status.REJECTED, pageable);

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
    }

    @Test
    void whenTryToGetBookingsByOwnerIdByNotExistsUserThenUserNotFoundException() {
        Mockito.when(itemRepository.existsByOwnerId(3L))
                .thenReturn(false);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getBookingsByOwnerId(3L, BookingState.ALL, 0, 10));

        Assertions.assertEquals("User with id=3 is not the owner of any thing", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
    }

    @Test
    void whenGetBookingsByOwnerIdAndStateAllThenCallGetAllByBookerIdBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByOwnerId(1L, pageable))
                .thenReturn(bookings);

        bookingService.getBookingsByOwnerId(1L, BookingState.ALL, 0, 10);

        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByOwnerId(1L, pageable);

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetBookingsByOwnerIdAndStateCurrentThenCallGetAllCurrentByBookerIdBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getBookingsByOwnerId(1L, BookingState.CURRENT, 0, 10);

        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetBookingsByOwnerAndStateFutureThenCallGetAllByBookerIdAndStartAfterBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getBookingsByOwnerId(1L, BookingState.FUTURE, 0, 10);

        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetBookingsByOwnerAndStatePastThenCallGetAllByBookerIdAndStartAfterBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getBookingsByOwnerId(1L, BookingState.PAST, 0, 10);

        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetBookingsByOwnerAndStateWaitingThenCallGetAllByBookerIdAndStartAfterBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByOwnerIdAndStatus(1L, Status.WAITING, pageable))
                .thenReturn(bookings);

        bookingService.getBookingsByOwnerId(1L, BookingState.WAITING, 0, 10);

        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByOwnerIdAndStatus(1L, Status.WAITING, pageable);

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
    }

    @Test
    void whenGetBookingsByOwnerAndStateRejectedThenCallGetAllByBookerIdAndStartAfterBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByOwnerIdAndStatus(1L, Status.REJECTED, pageable))
                .thenReturn(bookings);

        bookingService.getBookingsByOwnerId(1L, BookingState.REJECTED, 0, 10);

        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByOwnerIdAndStatus(1L, Status.REJECTED, pageable);

        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
    }
}