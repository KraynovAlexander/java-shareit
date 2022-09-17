package ru.practicum.shareit.booking.repo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Slice;

import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Pagination;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void getAllCurrentByBookerId() throws InterruptedException {
        User owner = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        User booker = User.builder()
                .name("booker")
                .email("booker@ya.ru")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(owner)
                .available(true)
                .description("good")
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(owner)
                .available(true)
                .description("another good")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(first)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(second)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusHours(7))
                .build();

        em.persist(owner);
        em.persist(booker);
        em.persist(first);
        em.persist(second);
        em.persist(bookingOne);
        em.persist(bookingTwo);

        Thread.sleep(3000L);

        Slice<Booking> bookings = bookingRepository.getAllCurrentByBookerId(booker.getId(), LocalDateTime.now(),
                Pagination.of(0, 5));

        Assertions.assertEquals(1, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingTwo));
    }

    @Test
    void getAllByOwnerId() {
        User owner = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        User secondOwner = User.builder()
                .name("else")
                .email("else@ya.ru")
                .build();
        User booker = User.builder()
                .name("booker")
                .email("booker@ya.ru")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(owner)
                .available(true)
                .description("good")
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(secondOwner)
                .available(true)
                .description("another good")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(first)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(4))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(second)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(5))
                .end(LocalDateTime.now().plusHours(7))
                .build();

        em.persist(owner);
        em.persist(secondOwner);
        em.persist(booker);
        em.persist(first);
        em.persist(second);
        em.persist(bookingOne);
        em.persist(bookingTwo);

        Slice<Booking> bookings = bookingRepository.getAllByOwnerId(owner.getId(), Pagination.of(0, 5));

        Assertions.assertEquals(1, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingOne));
    }

    @Test
    void getAllCurrentByOwnerId() throws InterruptedException {
        User owner = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        User booker = User.builder()
                .name("booker")
                .email("booker@ya.ru")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(owner)
                .available(true)
                .description("good")
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(owner)
                .available(true)
                .description("another good")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(first)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(second)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusHours(7))
                .build();

        em.persist(owner);
        em.persist(booker);
        em.persist(first);
        em.persist(second);
        em.persist(bookingOne);
        em.persist(bookingTwo);

        Thread.sleep(3000L);

        Slice<Booking> bookings = bookingRepository.getAllCurrentByOwnerId(owner.getId(), LocalDateTime.now(),
                Pagination.of(0, 5));

        Assertions.assertEquals(1, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingTwo));
    }

    @Test
    void getAllPastByOwnerId() throws InterruptedException {
        User owner = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        User booker = User.builder()
                .name("booker")
                .email("booker@ya.ru")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(owner)
                .available(true)
                .description("good")
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(owner)
                .available(true)
                .description("another good")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(first)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(second)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        em.persist(owner);
        em.persist(booker);
        em.persist(first);
        em.persist(second);
        em.persist(bookingOne);
        em.persist(bookingTwo);

        Thread.sleep(3000L);

        Slice<Booking> bookings = bookingRepository.getAllPastByOwnerId(owner.getId(), LocalDateTime.now(),
                Pagination.of(0, 5));

        Assertions.assertEquals(2, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingOne));
        assertThat(bookings.getContent().get(1), equalTo(bookingTwo));
    }

    @Test
    void getAllFutureByOwnerId() throws InterruptedException {
        User owner = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        User booker = User.builder()
                .name("booker")
                .email("booker@ya.ru")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(owner)
                .available(true)
                .description("good")
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(owner)
                .available(true)
                .description("another good")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(first)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(7))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(second)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        em.persist(owner);
        em.persist(booker);
        em.persist(first);
        em.persist(second);
        em.persist(bookingOne);
        em.persist(bookingTwo);

        Slice<Booking> bookings = bookingRepository.getAllFutureByOwnerId(owner.getId(), LocalDateTime.now(),
                Pagination.of(0, 5));

        Assertions.assertEquals(2, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingOne));
        assertThat(bookings.getContent().get(1), equalTo(bookingTwo));
    }

    @Test
    void getAllByOwnerIdAndStatus() {
        User owner = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        User booker = User.builder()
                .name("booker")
                .email("booker@ya.ru")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(owner)
                .available(true)
                .description("good")
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(owner)
                .available(true)
                .description("another good")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(first)
                .status(Status.REJECTED)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(7))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(second)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        em.persist(owner);
        em.persist(booker);
        em.persist(first);
        em.persist(second);
        em.persist(bookingOne);
        em.persist(bookingTwo);

        Slice<Booking> bookings = bookingRepository.getAllByOwnerIdAndStatus(owner.getId(), Status.REJECTED,
                Pagination.of(0, 5));

        Assertions.assertEquals(1, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingOne));
    }

    @Test
    void getAllByBookerId() {
        User owner = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        User booker = User.builder()
                .name("booker")
                .email("booker@ya.ru")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(owner)
                .available(true)
                .description("good")
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(owner)
                .available(true)
                .description("another good")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(first)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(4))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(second)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(5))
                .end(LocalDateTime.now().plusHours(7))
                .build();

        em.persist(owner);
        em.persist(booker);
        em.persist(first);
        em.persist(second);
        em.persist(bookingOne);
        em.persist(bookingTwo);

        Slice<Booking> bookings = bookingRepository.getAllByBookerId(booker.getId(), Pagination.of(0, 5));

        Assertions.assertEquals(2, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingOne));
        assertThat(bookings.getContent().get(1), equalTo(bookingTwo));
    }
}