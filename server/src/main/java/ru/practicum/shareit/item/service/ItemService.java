package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.dto.CommentDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemDtoFull;
import ru.practicum.shareit.item.model.dto.ItemDtoWithBookings;

import java.util.List;

public interface ItemService {

    ItemDto addNewItem(long userId, ItemDto item);

    CommentDto postComment(CommentDto commentDto, long userId, long itemId);

    ItemDto updateItem(long userId, long itemId, ItemDto item);

    ItemDtoFull findItemById(long userId, long itemId);

    List<ItemDtoWithBookings> getItemsByOwnerId(long userId, int from, int size);

    List<ItemDto> search(String text, int from, int size);
}
