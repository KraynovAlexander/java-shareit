package ru.practicum.shareit.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDto {
    private Long id;
    private Long userId;
    private String description;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime created;
}
