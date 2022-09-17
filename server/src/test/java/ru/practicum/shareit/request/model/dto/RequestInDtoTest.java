package ru.practicum.shareit.request.model.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class RequestInDtoTest {
    @Autowired
    JacksonTester<RequestInDto> json;

    @Test
    void testRequestInDto() throws IOException {
        RequestInDto requestDto = RequestInDto.builder()
                .id(8L)
                .description("I need book on java")
                .build();

        JsonContent<RequestInDto> result = json.write(requestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(8);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("I need book on java");
    }
}