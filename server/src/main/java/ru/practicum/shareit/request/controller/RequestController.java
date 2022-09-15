package ru.practicum.shareit.request.controller;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public RequestDto create(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                             @RequestBody RequestInDto requestInDto) {
        return requestService.addNewRequest(userId, requestInDto);
    }

    @GetMapping
    public List<RequestDtoWithItems> getAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.findRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public List<RequestDtoWithItems> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @RequestParam(value = "from") int from,
                                            @RequestParam(value = "size") int size) {
        return requestService.findAllAnotherUsersRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestDtoWithItems getByRequestId(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable long requestId) {
        return requestService.getById(userId, requestId);
    }
}
