package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.info("Создание элемента {}, userId={}", itemDto, userId);
        return itemClient.create(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @Valid @RequestBody CommentDto commentDto,
                                              @PathVariable @Positive long itemId) {
        log.info("Создание комментария {}, userId={}, itemId={}", commentDto, userId, itemId);
        return itemClient.postComment(userId, commentDto, itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestBody ItemDto itemDto,
                                         @PathVariable @Positive long itemId) {
        log.info("Обновление элемента, itemId={}, userId={}, itemDto={}", itemId, userId, itemDto);
        return itemClient.update(userId, itemDto, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getByItemId(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable @Positive long itemId) {
        log.info("Получение товара {}, userId={}", itemId, userId);
        return itemClient.getByItemId(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(value = "from", required = false, defaultValue = "0")
                                                  @PositiveOrZero int from,
                                              @RequestParam(value = "size", required = false, defaultValue = "10")
                                                  @Min(1) int size) {
        log.info("Получение предметов с помощью userId={}, from={}, size={}", userId, from, size);
        return itemClient.getByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(value = "text") String text,
                                         @RequestParam(value = "from", required = false, defaultValue = "0")
                                             @PositiveOrZero int from,
                                         @RequestParam(value = "size", required = false, defaultValue = "10")
                                             @Min(1) int size) {
        log.info("Получение элементов по содержанию текста в названии и/или в описании ={}, from={}, size={}",
                text, from, size);
        return itemClient.search(text, from, size);
    }
}
