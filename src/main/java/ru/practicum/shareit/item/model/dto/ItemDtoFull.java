package ru.practicum.shareit.item.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ru.practicum.shareit.booking.model.dto.BookingShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDtoFull {
    private Long id;
    private Long ownerId;
    @NotNull(message = "Имя не может быть нулевым")
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @NotNull(message = "Описание не может быть нулевым")
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    @NotNull(message = "Доступный не может быть нулевым")
    private Boolean available;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments;
}