package ru.practicum.shareit.item.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.dto.BookingShortDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class TestItemDtoWithBookings {
    @Autowired
    JacksonTester<ItemDtoWithBookings> json;

    @Test
    void testItemDtoWithBookings() throws IOException {
        LocalDateTime lastStart = LocalDateTime.now().minusDays(2);
        LocalDateTime lastEnd = LocalDateTime.now().minusDays(1);
        LocalDateTime nextStart = LocalDateTime.now().plusDays(1);
        LocalDateTime nextEnd = LocalDateTime.now().plusDays(2);

        BookingShortDto last = BookingShortDto.builder()
                .id(3L)
                .bookerId(2L)
                .start(lastStart)
                .end(lastEnd)
                .build();

        BookingShortDto next = BookingShortDto.builder()
                .id(4L)
                .bookerId(2L)
                .start(nextStart)
                .end(nextEnd)
                .build();

        ItemDtoWithBookings itemDto = ItemDtoWithBookings.builder()
                .id(11L)
                .name("book")
                .description("on java")
                .available(true)
                .ownerId(1L)
                .itemRequestId(10L)
                .lastBooking(last)
                .nextBooking(next)
                .build();

        JsonContent<ItemDtoWithBookings> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(11);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("book");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("on java");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemRequestId").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(3);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start")
                .isEqualTo(lastStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.end")
                .isEqualTo(lastEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(4);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.start")
                .isEqualTo(nextStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.end")
                .isEqualTo(nextEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}