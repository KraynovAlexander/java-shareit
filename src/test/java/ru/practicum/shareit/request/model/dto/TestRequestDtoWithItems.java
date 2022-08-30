package ru.practicum.shareit.request.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.model.dto.ItemDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class TestRequestDtoWithItems {
    @Autowired
    JacksonTester<RequestDtoWithItems> json;

    @Test
    void testRequestDto() throws IOException {
        LocalDateTime created = LocalDateTime.now();
        ItemDto firstItem = ItemDto.builder()
                .id(6L)
                .requestId(7L)
                .description("the book Thinking on java")
                .available(true)
                .ownerId(5L)
                .build();
        ItemDto secondItem = ItemDto.builder()
                .id(5L)
                .requestId(7L)
                .description("some interesting book")
                .available(false)
                .ownerId(4L)
                .build();

        RequestDtoWithItems requestDto = RequestDtoWithItems.builder()
                .id(7L)
                .description("I need book on java")
                .created(created)
                .items(List.of(firstItem, secondItem))
                .build();

        JsonContent<RequestDtoWithItems> result = json.write(requestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(7);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("I need book on java");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(6);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(7);
        assertThat(result).extractingJsonPathStringValue("$.items[0].description")
                .isEqualTo("the book Thinking on java");
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(5);
        assertThat(result).extractingJsonPathNumberValue("$.items[1].id").isEqualTo(5);
        assertThat(result).extractingJsonPathNumberValue("$.items[1].requestId").isEqualTo(7);
        assertThat(result).extractingJsonPathStringValue("$.items[1].description")
                .isEqualTo("some interesting book");
        assertThat(result).extractingJsonPathBooleanValue("$.items[1].available").isEqualTo(false);
        assertThat(result).extractingJsonPathNumberValue("$.items[1].ownerId").isEqualTo(4);
    }
}