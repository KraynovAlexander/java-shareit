package ru.practicum.shareit.item;


import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(long userId, ItemDto item);
    ItemDto updateItem(long userId, long itemId, ItemDto item);
    ItemDto findItemById(long itemId);
    List<ItemDto> getItemsByOwnerId(long userId);
    List<ItemDto> search(String text);
}