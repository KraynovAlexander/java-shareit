package ru.practicum.shareit.booking.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class TestBookingInDto {
    @Autowired
    JacksonTester<BookingInDto> json;

    @Test
    void testBookingInDto() throws IOException {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingInDto bookingInDto = BookingInDto.builder()
                .id(11L)
                .itemId(6L)
                .bookerId(1L)
                .start(start)
                .end(end)
                .status("WAITING")
                .build();

        JsonContent<BookingInDto> result = json.write(bookingInDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(11);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(6);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }
}