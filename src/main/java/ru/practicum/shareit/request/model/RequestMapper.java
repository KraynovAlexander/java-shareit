package ru.practicum.shareit.request.model;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

public class RequestMapper {

    public static RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .description(request.getDescription())
                .created(request.getCreationTime())
                .build();
    }

    public static Request toRequest(RequestInDto requestInDto, LocalDateTime localDateTime, User user) {
        return Request.builder()
                .id(requestInDto.getId())
                .description(requestInDto.getDescription())
                .creationTime(localDateTime)
                .user(user)
                .build();
    }

    public static RequestDtoWithItems toRequestDtoWithItems(Request request, List<Item> items) {
        return RequestDtoWithItems.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreationTime())
                .items(items.stream()
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList()))
                .build();
    }
}