package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.InvalidRequestException;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
    }

    public ResponseEntity<Object> addNewBooking(long userId, BookingDto bookingDto) {
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()))
            throw new InvalidRequestException("начало бронирования должно быть раньше, чем окончание");

        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> updateStatus(long userId, long bookingId, boolean isApproved) {
        Map<String, Object> parameters = Map.of(
                "approved", isApproved
        );

        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getById(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getUserBookings(long userId, String stateParam, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", stateParam,
                "from", from,
                "size", size
        );

        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getBookingsByOwnerId(long userId, String stateParam, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", stateParam,
                "from", from,
                "size", size
        );

        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }
}