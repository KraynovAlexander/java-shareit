package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.dto.CommentDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemDtoFull;
import ru.practicum.shareit.item.model.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader(value = "X-Sharer-User-Id") long userId, @RequestBody ItemDto item) {
        return itemService.addNewItem(userId, item);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(@RequestHeader("X-Sharer-User-Id") long userId, @RequestBody CommentDto commentDto,
                                  @PathVariable long itemId) {
        return itemService.postComment(commentDto, userId, itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId, @RequestBody ItemDto item,
                          @PathVariable long itemId) {
        return itemService.updateItem(userId, itemId, item);
    }

    @GetMapping("/{itemId}")
    public ItemDtoFull getByItemId(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        return itemService.findItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoWithBookings> getByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(value = "from") int from,
                                                 @RequestParam(value = "size") int size) {
        return itemService.getItemsByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(value = "text") String text,
                                @RequestParam(value = "from") int from,
                                @RequestParam(value = "size") int size) {
        return itemService.search(text, from, size);
    }
}
