package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class TestBookingServiceImpl {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private static UserDto userDtoItemOwner;
    private static UserDto userDtoBooker;
    private static ItemDto itemDto;
    private static BookingInDto bookingInDto;

    @BeforeAll
    public static void setUp() {
        userDtoItemOwner = UserDto.builder()
                .email("user@yandex.ru")
                .name("userName")
                .build();

        userDtoBooker = UserDto.builder()
                .name("booker")
                .email("booker@ya.ru")
                .build();

        itemDto = ItemDto.builder()
                .name("book")
                .description("JAVA reference")
                .available(true)
                .build();

        bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusHours(1L))
                .end(LocalDateTime.now().plusDays(2L))
                .build();
    }

    @Test
    void addNewBooking() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());

        BookingOutDto returned = bookingService.addNewBooking(booker.getId(), bookingInDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        Booking booking = query
                .setParameter("bookingId", returned.getId())
                .getSingleResult();

        assertThat(returned.getId(), notNullValue());
        assertThat(returned.getStatus(), equalTo(booking.getStatus().name()));
        assertThat(returned.getItem(), equalTo(ItemMapper.toItem(item, UserMapper.toUser(itemOwner))));
        assertThat(returned.getBooker(), equalTo(UserMapper.toUser(booker)));
    }

    @Test
    void updateStatus() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        BookingOutDto returned = bookingService.addNewBooking(booker.getId(), bookingInDto);

        BookingOutDto updated = bookingService.updateStatus(itemOwner.getId(), returned.getId(), true);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        Booking booking = query
                .setParameter("bookingId", returned.getId())
                .getSingleResult();

        assertThat(updated.getId(), equalTo(returned.getId()));
        assertThat(updated.getStatus(), equalTo(Status.APPROVED.name()));
        assertThat(booking.getStatus(), equalTo(Status.APPROVED));
        assertThat(updated.getItem(), equalTo(ItemMapper.toItem(item, UserMapper.toUser(itemOwner))));
        assertThat(updated.getBooker(), equalTo(UserMapper.toUser(booker)));
    }

    @Test
    void getById() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        BookingOutDto returned = bookingService.addNewBooking(booker.getId(), bookingInDto);

        BookingOutDto found = bookingService.getById(booker.getId(), returned.getId());

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        Booking booking = query
                .setParameter("bookingId", returned.getId())
                .getSingleResult();

        assertThat(found.getStatus(), equalTo(booking.getStatus().name()));
        assertThat(found.getBooker(), equalTo(booking.getBooker()));
        assertThat(found.getItem(), equalTo(booking.getItem()));
        assertThat(found.getStart(), equalTo(booking.getStart()));
        assertThat(found.getEnd(), equalTo(booking.getEnd()));
    }

    @Test
    void getUserBookingsAll() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto bookingOfBooker = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        BookingOutDto first = bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto second = bookingService.addNewBooking(booker.getId(), bookingOfBooker);

        List<BookingOutDto> bookerBookings = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);

        assertThat(bookerBookings.size(), equalTo(2));
        assertThat(bookerBookings.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(0), equalTo(second));
        assertThat(bookerBookings.get(1).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(1), equalTo(first));
    }

    @Test
    void getUserBookingsCurrent() throws InterruptedException {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto currentDto = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto returnedCurrent = bookingService.addNewBooking(booker.getId(), currentDto);

        Thread.sleep(1000L);
        List<BookingOutDto> bookerBookings = bookingService.getUserBookings(booker.getId(), "CURRENT", 0, 10);

        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(0), equalTo(returnedCurrent));
    }

    @Test
    void getUserBookingsPast() throws InterruptedException {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto pastDto = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();
        bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto returnedPast = bookingService.addNewBooking(booker.getId(), pastDto);

        Thread.sleep(2000L);
        List<BookingOutDto> bookerBookings = bookingService.getUserBookings(booker.getId(), "PAST", 0, 10);

        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(0), equalTo(returnedPast));
    }

    @Test
    void getUserBookingsFuture() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto anotherFutureBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        BookingOutDto firstFuture = bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto secondFuture = bookingService.addNewBooking(booker.getId(), anotherFutureBooking);

        List<BookingOutDto> bookerBookings = bookingService.getUserBookings(booker.getId(), "FUTURE", 0, 10);

        assertThat(bookerBookings.size(), equalTo(2));
        assertThat(bookerBookings.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(0), equalTo(secondFuture));
        assertThat(bookerBookings.get(1).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(1), equalTo(firstFuture));
    }

    @Test
    void getUserBookingsRejected() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto bookingOfBooker = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        BookingOutDto first = bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto second = bookingService.addNewBooking(booker.getId(), bookingOfBooker);
        BookingOutDto updatedToRejected = bookingService.updateStatus(itemOwner.getId(), second.getId(), false);

        List<BookingOutDto> bookerBookings = bookingService.getUserBookings(booker.getId(), "REJECTED", 0, 10);

        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(0), equalTo(updatedToRejected));
        assertThat(bookerBookings.get(0).getStatus(), equalTo(Status.REJECTED.name()));
    }

    @Test
    void getUserBookingsWaiting() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto bookingOfBooker = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        BookingOutDto first = bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto second = bookingService.addNewBooking(booker.getId(), bookingOfBooker);
        bookingService.updateStatus(itemOwner.getId(), second.getId(), true);

        List<BookingOutDto> bookerBookings = bookingService.getUserBookings(booker.getId(), "WAITING", 0, 10);

        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(0), equalTo(first));
        assertThat(bookerBookings.get(0).getStatus(), equalTo(Status.WAITING.name()));
    }

    @Test
    void getBookingsByOwnerIdAll() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto bookingOfBooker = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        BookingOutDto first = bookingService.addNewBooking(booker.getId(), bookingInDto);
        BookingOutDto second = bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto third = bookingService.addNewBooking(booker.getId(), bookingOfBooker);

        List<BookingOutDto> bookerBookings = bookingService.getBookingsByOwnerId(itemOwner.getId(), "ALL", 0, 10);

        assertThat(bookerBookings.size(), equalTo(3));
        assertThat(bookerBookings.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(0), equalTo(third));
        assertThat(bookerBookings.get(1).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(1), equalTo(second));
        assertThat(bookerBookings.get(2).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(2), equalTo(first));
    }

    @Test
    void getBookingsByOwnerIdCurrent() throws InterruptedException {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto currentDto = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto returnedCurrent = bookingService.addNewBooking(booker.getId(), currentDto);

        Thread.sleep(1000L);
        List<BookingOutDto> bookerBookings = bookingService.getBookingsByOwnerId(itemOwner.getId(), "CURRENT", 0, 10);

        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(0), equalTo(returnedCurrent));
    }

    @Test
    void getBookingsByOwnerIdPast() throws InterruptedException {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto pastDto = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();
        bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto returnedPast = bookingService.addNewBooking(booker.getId(), pastDto);

        Thread.sleep(2000L);
        List<BookingOutDto> bookerBookings = bookingService.getBookingsByOwnerId(itemOwner.getId(), "PAST", 0, 10);

        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(0), equalTo(returnedPast));
    }

    @Test
    void getBookingsByOwnerIdFuture() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto anotherFutureBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        BookingOutDto firstFuture = bookingService.addNewBooking(booker.getId(), bookingInDto);
        BookingOutDto secondFuture = bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto thirdFuture = bookingService.addNewBooking(booker.getId(), anotherFutureBooking);

        List<BookingOutDto> bookerBookings = bookingService.getBookingsByOwnerId(itemOwner.getId(), "FUTURE", 0, 10);

        assertThat(bookerBookings.size(), equalTo(3));
        assertThat(bookerBookings.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(0), equalTo(thirdFuture));
        assertThat(bookerBookings.get(1).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(1), equalTo(secondFuture));
        assertThat(bookerBookings.get(2).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(2), equalTo(firstFuture));
    }

    @Test
    void getBookingsByOwnerIdRejected() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto bookingOfBooker = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        bookingService.addNewBooking(booker.getId(), bookingInDto);
        bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto second = bookingService.addNewBooking(booker.getId(), bookingOfBooker);
        BookingOutDto updatedToRejected = bookingService.updateStatus(itemOwner.getId(), second.getId(), false);

        List<BookingOutDto> bookerBookings = bookingService.getBookingsByOwnerId(itemOwner.getId(), "REJECTED", 0, 10);

        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(0), equalTo(updatedToRejected));
        assertThat(bookerBookings.get(0).getStatus(), equalTo(Status.REJECTED.name()));
    }

    @Test
    void getBookingsByOwnerIdWaiting() {
        UserDto itemOwner = userService.save(userDtoItemOwner);
        ItemDto item = itemService.addNewItem(itemOwner.getId(), itemDto);
        UserDto booker = userService.save(userDtoBooker);
        bookingInDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("anotherBooker")
                .email("anotherBooker@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.save(anotherBooker);
        BookingInDto anotherBooking = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingInDto bookingOfBooker = BookingInDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        BookingOutDto first = bookingService.addNewBooking(booker.getId(), bookingInDto);
        BookingOutDto second = bookingService.addNewBooking(returnedAnotherBooker.getId(), anotherBooking);
        BookingOutDto third = bookingService.addNewBooking(booker.getId(), bookingOfBooker);
        bookingService.updateStatus(itemOwner.getId(), first.getId(), true);
        bookingService.updateStatus(itemOwner.getId(), third.getId(), true);

        List<BookingOutDto> bookerBookings = bookingService.getBookingsByOwnerId(itemOwner.getId(), "WAITING", 0, 10);

        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(bookerBookings.get(0), equalTo(second));
        assertThat(bookerBookings.get(0).getStatus(), equalTo(Status.WAITING.name()));
    }
}