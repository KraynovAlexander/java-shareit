package ru.practicum.shareit.item.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemResponseDtoTest {
    @Autowired
    JacksonTester<ItemResponseDto> json;

    @Test
    void testItemResponseDto() throws IOException {
        ItemResponseDto itemDto = ItemResponseDto.builder()
                .id(11L)
                .name("book")
                .description("on java")
                .ownerId(1L)
                .build();

        JsonContent<ItemResponseDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(11);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("book");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("on java");
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(1);
    }
}