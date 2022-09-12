package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.RequestDto;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class RequestDtoTest {
    @Autowired
    JacksonTester<RequestDto> json;

    @Test
    void testRequestDto() throws IOException {
        RequestDto requestDto = RequestDto.builder()
                .description("I need book on java")
                .build();

        JsonContent<RequestDto> result = json.write(requestDto);

        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("I need book on java");
    }
}