package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public RequestDto create(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                             @Valid @RequestBody RequestInDto requestInDto) {
        log.info(" RequestDto create для пользователя с id={} успешно выполнен! ", userId);
        return requestService.addNewRequest(userId, requestInDto);
    }

    @GetMapping
    public List<RequestDtoWithItems> getAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Список getAllByUserId для пользователь с id={} успешно создан! ",userId);
        return requestService.findRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public List<RequestDtoWithItems> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @RequestParam(value = "from", required = false, defaultValue = "0")
                                            @PositiveOrZero int from,
                                            @RequestParam(value = "size", required = false, defaultValue = "10")
                                            @Positive @Min(1) int size) {
        log.info("Список getAll  для пользователь с id={} успешно создан! ",userId);
        return requestService.findAllAnotherUsersRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestDtoWithItems getByRequestId(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable long requestId) {
        log.info("RequestDtoWithItems getByRequestId для пользователя с id={} успешно выполнен! ", userId);
        return requestService.getById(userId, requestId);
    }
}