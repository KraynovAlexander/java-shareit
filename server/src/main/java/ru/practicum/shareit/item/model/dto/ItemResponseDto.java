package ru.practicum.shareit.item.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDto {
    private long id;
    private String name;
    private String description;
    private long ownerId;
}
