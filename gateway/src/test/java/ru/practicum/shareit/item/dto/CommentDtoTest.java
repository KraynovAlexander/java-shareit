package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class CommentDtoTest {
    @Autowired
    JacksonTester<CommentDto> json;

    @Test
    void testCommentDto() throws IOException {
        LocalDateTime created = LocalDateTime.now();

        CommentDto commentDto = CommentDto.builder()
                .text("very good")
                .authorName("author")
                .build();

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("very good");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("author");
    }
}