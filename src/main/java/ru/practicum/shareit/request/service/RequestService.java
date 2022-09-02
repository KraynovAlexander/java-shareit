package ru.practicum.shareit.request.service;


import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;

import java.util.List;

public interface RequestService {
    RequestDto addNewRequest(long userId, RequestInDto requestInDto);

    List<RequestDtoWithItems> findRequestsByUserId(long userId);

    List<RequestDtoWithItems> findAllAnotherUsersRequests(long userId, int from, int size);

    RequestDtoWithItems getById(long userId, long requestId);
}