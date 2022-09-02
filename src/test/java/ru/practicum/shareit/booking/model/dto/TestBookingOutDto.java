package ru.practicum.shareit.booking.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.model.Item;

import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class TestBookingOutDto {
    @Autowired
    JacksonTester<BookingOutDto> json;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd, hh:mm:ss");

    @Test
    void testBookingOutDto() throws IOException {
        LocalDateTime creationTime = LocalDateTime.now();

        User booker = User.builder()
                .id(20L)
                .name("Booker")
                .email("booker@ya.ru")
                .build();

        User owner = User.builder()
                .id(21L)
                .name("Owner")
                .email("owner@gmail.com")
                .build();

        Request request = Request.builder()
                .id(3L)
                .description("I need book on java")
                .user(booker)
                .creationTime(creationTime)
                .build();

        Item item = Item.builder()
                .id(38L)
                .name("book")
                .description("on java")
                .available(true)
                .request(request)
                .owner(owner)
                .build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingOutDto bookingOutDto = BookingOutDto.builder()
                .id(11L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status("APPROVED")
                .build();

        JsonContent<BookingOutDto> result = json.write(bookingOutDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(11);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(38);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("book");
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo("on java");
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.item.request.id").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.item.request.description")
                .isEqualTo("I need book on java");
        assertThat(result).extractingJsonPathNumberValue("$.item.request.user.id").isEqualTo(20);
        assertThat(result).extractingJsonPathStringValue("$.item.request.user.name")
                .isEqualTo("Booker");
        assertThat(result).extractingJsonPathStringValue("$.item.request.user.email")
                .isEqualTo("booker@ya.ru");
        assertThat(result).extractingJsonPathStringValue("$.item.request.creationTime")
                .isEqualTo(creationTime.format(DATE_FORMAT));
        assertThat(result).extractingJsonPathNumberValue("$.item.owner.id").isEqualTo(21);
        assertThat(result).extractingJsonPathStringValue("$.item.owner.name")
                .isEqualTo("Owner");
        assertThat(result).extractingJsonPathStringValue("$.item.owner.email")
                .isEqualTo("owner@gmail.com");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }
}