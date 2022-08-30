package ru.practicum.shareit.request.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.utils.Constants;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public RequestDto create(@RequestHeader(value = Constants.SHARER) long userId,
                             @Valid @RequestBody RequestInDto requestInDto) {
        return requestService.addNewRequest(userId, requestInDto);
    }

    @GetMapping
    public List<RequestDtoWithItems> getAllByUserId(@RequestHeader(Constants.SHARER) long userId) {
        return requestService.findRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public List<RequestDtoWithItems> getAll(@RequestHeader(Constants.SHARER) long userId,
                                            @RequestParam(value = "from", required = false, defaultValue = "0")
                                            @PositiveOrZero int from,
                                            @RequestParam(value = "size", required = false, defaultValue = "10")
                                            @Positive @Min(1) int size) {
        return requestService.findAllAnotherUsersRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestDtoWithItems getByRequestId(@RequestHeader(Constants.SHARER) long userId,
                                              @PathVariable long requestId) {
        return requestService.getById(userId, requestId);
    }
}