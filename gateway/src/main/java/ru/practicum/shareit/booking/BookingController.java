package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.validator.ValueOfEnum;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addNewBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestBody @Valid BookingDto bookingDto) {
        log.info("Создание бронирования {}, userId={}", bookingDto, userId);
        return bookingClient.addNewBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateStatus(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @PathVariable @Positive long bookingId,
                                               @RequestParam(value = "approved") boolean isApproved) {
        log.info("Обновление бронирования{}, userId={}, обновленный статус isApproved={}", bookingId, userId, isApproved);
        return bookingClient.updateStatus(userId, bookingId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable @Positive Long bookingId) {
        log.info("Получение бронирования {}, userId={}", bookingId, userId);
        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(value = "state", required = false, defaultValue = "ALL")
                                                  @ValueOfEnum(enumClass = BookingState.class, isNullEnabled = true)
                                                  String stateParam,
                                                  @RequestParam(value = "from", required = false, defaultValue = "0")
                                                  @PositiveOrZero int from,
                                                  @RequestParam(value = "size", required = false, defaultValue = "10")
                                                  @Min(1) int size) {
        log.info("Получение пользовательских бронирований с государственным {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getUserBookings(userId, stateParam, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                       @RequestParam(name = "state", defaultValue = "ALL")
                                                       @ValueOfEnum(enumClass = BookingState.class, isNullEnabled = true)
                                                       String stateParam,
                                                       @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero
                                                       Integer from,
                                                       @RequestParam(name = "size", defaultValue = "10") @Min(1)
                                                       Integer size) {
        log.info("Получение бронирований с помощью ownerId={} with state {}, from={}, size={}", ownerId, stateParam, from, size);
        return bookingClient.getBookingsByOwnerId(ownerId, stateParam, from, size);
    }
}