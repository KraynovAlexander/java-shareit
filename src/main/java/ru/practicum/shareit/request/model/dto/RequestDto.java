package ru.practicum.shareit.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDto {
    private Long id;
    private Long userId;
    @NotNull(message = "Описание не может быть нулевым")
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime created;
}