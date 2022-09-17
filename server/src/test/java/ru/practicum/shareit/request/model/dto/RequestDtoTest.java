package ru.practicum.shareit.request.model.dto;

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
class RequestDtoTest {
    @Autowired
    JacksonTester<RequestDto> json;

    @Test
    void testRequestDto() throws IOException {
        LocalDateTime created = LocalDateTime.now();

        RequestDto requestDto = RequestDto.builder()
                .id(8L)
                .userId(1L)
                .description("I need book on java")
                .created(created)
                .build();

        JsonContent<RequestDto> result = json.write(requestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(8);
        assertThat(result).extractingJsonPathNumberValue("$.userId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("I need book on java");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}