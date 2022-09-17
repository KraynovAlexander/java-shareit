package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class UserDtoTest {
    @Autowired
    JacksonTester<UserDto> json;

    @Test
    void testUserDto() throws IOException {
        UserDto userDto = UserDto.builder()
                .name("User")
                .email("user@gmail.com")
                .build();

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("User");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user@gmail.com");
    }
}