package ru.practicum.shareit.request;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody RequestDto requestDto) {
        log.info("Создание запроса {}, userId={}", requestDto, userId);
        return requestClient.create(userId, requestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getByRequestId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable @Positive long requestId) {
        log.info("Получение запросов с помощью requestId={}, userId={}", requestId, userId);
        return requestClient.getByRequestId(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Получение запросов by userId={}", userId);
        return requestClient.getAllByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam(value = "from", required = false, defaultValue = "0")
                                         @PositiveOrZero int from,
                                         @RequestParam(value = "size", required = false, defaultValue = "10")
                                         @Positive @Min(1) int size) {
        log.info("Получение всех запросов, from={}, size={}", from, size);
        return requestClient.getAll(userId, from, size);
    }
}