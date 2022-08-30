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

import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.mapper.BookingMapper;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.errorHandler.exception.BookingException;
import ru.practicum.shareit.errorHandler.exception.RequestException;
import ru.practicum.shareit.errorHandler.exception.ItemException;
import ru.practicum.shareit.errorHandler.exception.UserNotFoundException;
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
class TestBookingService {
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
    private Request request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@ya.ru")
                .build();

        request = Request.builder()
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
    void whenTryToAddNewBookingOfNotExistsItemThenItemException() {
        Mockito.when(itemRepository.findById(bookingInDto.getItemId()))
                .thenThrow(new ItemException("Предмет с id=1 не найден"));

        final ItemException exception = Assertions.assertThrows(
                ItemException.class,
                () -> bookingService.addNewBooking(1L, bookingInDto));

        Assertions.assertEquals("Предмет с id=1 не найден", exception.getMessage());

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
                .thenThrow(new UserNotFoundException("Пользователь с  id=1 не найден"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.addNewBooking(1L, bookingInDto));

        Assertions.assertEquals("Пользователь с  id=1 не найден", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryToBookOwnItemThenItemException() {
        Mockito.when(itemRepository.findById(bookingInDto.getItemId()))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        final ItemException exception = Assertions.assertThrows(
                ItemException.class,
                () -> bookingService.addNewBooking(1L, bookingInDto));

        Assertions.assertEquals("пользователь пытается забронировать свой собственный товар", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryToBookNotAvailableItemThenRequestException() {
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
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        final RequestException exception = Assertions.assertThrows(
                RequestException.class,
                () -> bookingService.addNewBooking(2L, bookingInDto));

        Assertions.assertEquals("попытка бронирования не удалась из-за неверных данных", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryToAddBookingStartIsBeforeNowThenRequestException() {
        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().minusHours(2))
                .itemId(1L)
                .build();

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        final RequestException exception = Assertions.assertThrows(
                RequestException.class,
                () -> bookingService.addNewBooking(2L, bookingInDto));

        Assertions.assertEquals("попытка бронирования не удалась из-за неверных данных", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryToAddBookingStartIsAfterEndNowThenRequestException() {
        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(1))
                .itemId(1L)
                .build();

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        final RequestException exception = Assertions.assertThrows(
                RequestException.class,
                () -> bookingService.addNewBooking(2L, bookingInDto));

        Assertions.assertEquals("попытка бронирования не удалась из-за неверных данных", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
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
    void whenTryToUpdateStatusOfNotExistsBookingThenBookingException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenThrow(new BookingException("Бронирование с помощью id=1 не может быть"));

        final BookingException exception = Assertions.assertThrows(
                BookingException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        Assertions.assertEquals("Бронирование с помощью id=1 не может быть", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void whenTryToUpdateStatusSomeoneElseBookingThenBookingException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        final BookingException exception = Assertions.assertThrows(
                BookingException.class,
                () -> bookingService.updateStatus(10L, 1L, true));

        Assertions.assertEquals("бронирование с помощью id=1 для пользователя с id=10 не может быть", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void whenTryToUpdateStatusWhenStatusAlreadyApprovedThenRequestException() {
        Booking alreadyApproved = Booking.builder()
                .id(1L)
                .item(item)
                .status(Status.APPROVED)
                .build();

        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(alreadyApproved));

        final RequestException exception = Assertions.assertThrows(
                RequestException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        Assertions.assertEquals("статус не может быть изменен", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void whenTryToUpdateStatusWhenStatusRejectedThenRequestException() {
        Booking alreadyApproved = Booking.builder()
                .id(1L)
                .item(item)
                .status(Status.REJECTED)
                .build();

        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(alreadyApproved));

        final RequestException exception = Assertions.assertThrows(
                RequestException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        Assertions.assertEquals("статус не может быть изменен", exception.getMessage());

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
    void whenTryToGetByIdNotExistsBookingThenBookingException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenThrow(new BookingException("Бронирование с помощью id=1 не может быть"));

        final BookingException exception = Assertions.assertThrows(
                BookingException.class,
                () -> bookingService.getById(1L, 1L));

        Assertions.assertEquals("Бронирование с помощью id=1 не может быть", exception.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    void whenTryToGetByIdByNotOwnerThenBookingException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        final BookingException exception = Assertions.assertThrows(
                BookingException.class,
                () -> bookingService.getById(10L, 1L));

        Assertions.assertEquals("бронирование с помощью id=1 для пользователя с id=10 не может быть", exception.getMessage());

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
    void whenTryToGetUserBookingsIfStateIsNotValidThenRequestException() {
        final RequestException exception = Assertions.assertThrows(
                RequestException.class,
                () -> bookingService.getUserBookings(1L, " ", 0, 10));

        Assertions.assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());

        Mockito.verify(userRepository, Mockito.never())
                .existsById(Mockito.anyLong());
    }

    @Test
    void whenTryToGetUserBookingsByNotExistsUserThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getUserBookings(1L, "ALL", 0, 10));

        Assertions.assertEquals("Пользователь с id=1 не найден", exception.getMessage());

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

        bookingService.getUserBookings(1L, "ALL", 0, 10);

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

        bookingService.getUserBookings(1L, "CURRENT", 0, 10);

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

        bookingService.getUserBookings(1L, "FUTURE", 0, 10);

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

        bookingService.getUserBookings(1L, "PAST", 0, 10);

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

        bookingService.getUserBookings(1L, "WAITING", 0, 10);

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

        bookingService.getUserBookings(1L, "REJECTED", 0, 10);

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
    void whenTryToGetBookingsByOwnerIdIfStateIsNotValidThenRequestException() {
        final RequestException exception = Assertions.assertThrows(
                RequestException.class,
                () -> bookingService.getBookingsByOwnerId(1L, "NONE", 0, 10));

        Assertions.assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.never())
                .existsByOwnerId(Mockito.anyLong());
    }

    @Test
    void whenTryToGetBookingsByOwnerIdByNotExistsUserThenUserNotFoundException() {
        Mockito.when(itemRepository.existsByOwnerId(3L))
                .thenReturn(false);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getBookingsByOwnerId(3L, "ALL", 0, 10));

        Assertions.assertEquals("Пользователь с id=3 не является владельцем чего-либо", exception.getMessage());

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

        bookingService.getBookingsByOwnerId(1L, "ALL", 0, 10);

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

        bookingService.getBookingsByOwnerId(1L, "CURRENT", 0, 10);

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

        bookingService.getBookingsByOwnerId(1L, "FUTURE", 0, 10);

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

        bookingService.getBookingsByOwnerId(1L, "PAST", 0, 10);

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

        bookingService.getBookingsByOwnerId(1L, "WAITING", 0, 10);

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

        bookingService.getBookingsByOwnerId(1L, "REJECTED", 0, 10);

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