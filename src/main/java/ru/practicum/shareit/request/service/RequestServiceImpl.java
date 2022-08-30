package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import ru.practicum.shareit.errorHandler.exception.RequestNotFoundException;
import ru.practicum.shareit.errorHandler.exception.UserNotFoundException;
import ru.practicum.shareit.item.repo.ItemRepository;

import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.model.RequestMapper;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.request.repo.RequestRepository;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.utils.Pagination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public RequestServiceImpl(RequestRepository requestRepository, UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public RequestDto addNewRequest(long userId, RequestInDto requestInDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        return RequestMapper.toRequestDto(requestRepository.save(RequestMapper.toRequest(requestInDto,
                LocalDateTime.now(), user)));
    }

    @Override
    public List<RequestDtoWithItems> findRequestsByUserId(long userId) {
        checkUser(userId);

        List<Request> requests = requestRepository.findAllByUserIdOrderByCreationTimeDesc(userId);

        return requests.stream()
                .map(request -> RequestMapper.toRequestDtoWithItems(request,
                        itemRepository.findItemsByRequestId(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDtoWithItems> findAllAnotherUsersRequests(long userId, int from, int size) {
        Pageable sortedByDateDesc = Pagination.of(from, size, Sort.by("creationTime").descending());

        return requestRepository.findAllOtherUsersRequests(userId, sortedByDateDesc).get()
                .map(request -> RequestMapper.toRequestDtoWithItems(request,
                        itemRepository.findItemsByRequestId(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public RequestDtoWithItems getById(long userId, long requestId) {
        checkUser(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(String.format("Запрос с id=%s не найден", requestId)));

        return RequestMapper.toRequestDtoWithItems(request, itemRepository.findItemsByRequestId(requestId));
    }

    private void checkUser(long userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(String.format("Пользователь с id=%s не найден", userId));
    }
}