package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.InvalidRequestException;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {
    @Autowired
    private BookingController bookingController;
    @MockBean
    private BookingClient bookingClient;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static BookingDto bookingDto;
    private static ResponseEntity<Object> responseIsOk;

    @BeforeAll
    public static void beforeAll() {
        bookingDto = BookingDto.builder()
                .bookerId(2L)
                .itemId(3L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        responseIsOk = ResponseEntity
                .status(HttpStatus.OK)
                .build();

    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void whenAddValidBookingThenStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.addNewBooking(2L, bookingDto))
                .thenReturn(responseIsOk);

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .addNewBooking(2L, bookingDto);
    }

    @Test
    void whenTryToAddBookingWithStartBeforeNowThenStatusIsBadRequest() throws Exception {
        BookingDto startBeforeNow = BookingDto.builder()
                .bookerId(2L)
                .itemId(3L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(startBeforeNow)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never())
                .addNewBooking(Mockito.anyLong(), Mockito.any(BookingDto.class));
    }

    @Test
    void whenTryToAddBookingWithNegativeItemIdThenStatusIsBadRequest() throws Exception {
        BookingDto negativeItemId = BookingDto.builder()
                .bookerId(2L)
                .itemId(-3L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(negativeItemId)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never())
                .addNewBooking(Mockito.anyLong(), Mockito.any(BookingDto.class));
    }

    @Test
    void whenTryToAddBookingWithNegativeBookerIdThenStatusIsBadRequest() throws Exception {
        BookingDto negativeBookerId = BookingDto.builder()
                .bookerId(-2L)
                .itemId(3L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(negativeBookerId)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never())
                .addNewBooking(Mockito.anyLong(), Mockito.any(BookingDto.class));
    }

    @Test
    void whenTryToAddBookingWithStartBeforeEndThenStatusIsBadRequest() throws Exception {
        Mockito
                .when(bookingClient.addNewBooking(2L, bookingDto))
                .thenThrow(new InvalidRequestException("the start of the booking must be earlier than the end"));

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidRequestException))
                .andExpect(result -> assertEquals("the start of the booking must be earlier than the end",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.times(1))
                .addNewBooking(2L, bookingDto);
    }

    @Test
    void whenUpdateValidStatusThenStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.updateStatus(1L, 1L, true))
                .thenReturn(responseIsOk);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .updateStatus(1L, 1L, true);
    }

    @Test
    void whenGetValidByIdThenStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.getById(1L, 1L))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .getById(1L, 1L);
    }

    @Test
    void whenGetByNegativeIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/-1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never())
                .getById(1L, -1L);
    }

    @Test
    void whenGetUserBookingsThenStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.getUserBookings(5L, "ALL", 0, 10))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .getUserBookings(5L, "ALL", 0, 10);
    }

    @Test
    void whenGetUserBookingsStateNotValidThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings?state=EVERY")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getUserBookings.stateParam: Unknown state: UNSUPPORTED_STATUS",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void whenGetUserBookingsAndInvalidFromThenIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=-12")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getUserBookings.from: must be greater than or equal to 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetUserBookingsAndInvalidSizeThenIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=12&size=-1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getUserBookings.size: must be greater than or equal to 1",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetBookingsByOwnerIdThenStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.getUserBookings(5L, "REJECTED", 0, 10))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/bookings/owner?state=REJECTED")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .getBookingsByOwnerId(5L, "REJECTED", 0, 10);
    }

    @Test
    void whenTryToGetBookingsByOwnerIdIfStateIsNotValidThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=MOSCOW")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getBookingsByOwnerId.stateParam: Unknown state: " +
                                "UNSUPPORTED_STATUS",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void whenTryToGetBookingsByOwnerIdIfStateIsNotSpecifiedThenStateIsAllStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.getBookingsByOwnerId(2L, "ALL", 0, 10))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .getBookingsByOwnerId(2L, "ALL", 0, 10);
    }

    @Test
    void whenGetBookingsByOwnerIdAndInvalidFromThenIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=-12")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getBookingsByOwnerId.from: must be greater than or equal to 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetBookingsByOwnerIdAndInvalidSizeThenIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=12&size=-1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getBookingsByOwnerId.size: must be greater than or equal to 1",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetBookingsByOwnerIdAndSizeIsZeroThenIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=12&size=0")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getBookingsByOwnerId.size: must be greater than or equal to 1",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }
}