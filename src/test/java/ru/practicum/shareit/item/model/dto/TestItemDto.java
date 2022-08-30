package ru.practicum.shareit.item.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class TestItemDto {
    @Autowired
    JacksonTester<ItemDto> json;

    @Test
    void testItemDto() throws IOException {
        ItemDto itemDto = ItemDto.builder()
                .id(9L)
                .name("book")
                .description("on java")
                .available(true)
                .ownerId(1L)
                .requestId(5L)
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(9);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("book");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("on java");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }
}