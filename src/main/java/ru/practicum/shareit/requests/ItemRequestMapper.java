package ru.practicum.shareit.requests;

import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .userId(itemRequest.getUser().getId())
                .description(itemRequest.getDescription())
                .creationTime(itemRequest.getCreationTime())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .user(user)
                .description(itemRequestDto.getDescription())
                .creationTime(itemRequestDto.getCreationTime())
                .build();
    }
}