package ru.practicum.shareit.request.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestInDto {
    private Long id;
    @NotNull(message = "Описание не может быть нулевым")
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
}