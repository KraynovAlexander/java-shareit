package ru.practicum.shareit.item.dto;

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
public class ItemDto {

    @NotNull(message = "Имя не должно быть пустым")
    @NotBlank(message = "Имя не должно быть пустым")
    private String name;
    @NotNull(message = "Описание не должно быть нулевым")
    @NotBlank(message = "Описание не должно быть пустым")
    private String description;
    @NotNull(message = "Доступный не должен быть нулевым")
    private Boolean available;
    private Long requestId;
}