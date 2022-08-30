package ru.practicum.shareit.request.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;


import ru.practicum.shareit.errorHandler.exception.RequestNotFoundException;
import ru.practicum.shareit.errorHandler.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.item.repo.ItemRepository;

import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.model.RequestMapper;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.request.repo.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class TestRequestService {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RequestRepository requestRepository;
    @InjectMocks
    private RequestServiceImpl requestService;
    private RequestInDto requestInDto;
    private User user;
    private Request request;
    private Item item;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@ya.ru")
                .build();

        request = Request.builder()
                .id(1L)
                .user(user)
                .description("wanted")
                .build();

        item = Item.builder()
                .request(request)
                .owner(user)
                .build();

        requestInDto = RequestInDto.builder().description("looking for a book on java").build();
    }

    @Test
    void whenTryAddNewRequestByNotExistsUserThenUserNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenThrow(new UserNotFoundException("Пользователь с id=1 не найден"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.addNewRequest(1L, requestInDto));

        Assertions.assertEquals("Пользователь с id=1 не найден", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
    }

    @Test
    void whenAddNewRequestThenCallSaveRequestRepositoryAndReturnDtoWith() {
        request.setCreationTime(LocalDateTime.now());
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.save(Mockito.any(Request.class)))
                .thenReturn(request);

        RequestDto returned = requestService.addNewRequest(1L, requestInDto);

        assertThat(returned, equalTo(RequestMapper.toRequestDto(request)));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .save(Mockito.any(Request.class));
    }

    @Test
    void whenTryToFindRequestsByNotExistsUserIdThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.findRequestsByUserId(1L));

        Assertions.assertEquals("Пользователь с id=1 не найден", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void whenFindRequestsByUserIdThenReturnRequestsWithItems() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(requestRepository.findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong()))
                .thenReturn(List.of(request));
        Mockito.when(itemRepository.findItemsByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        List<RequestDtoWithItems> returned = requestService.findRequestsByUserId(1L);

        Assertions.assertNotNull(returned.get(0).getItems());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void whenFindThreeRequestsByUserIdThenCallThriceFindItemsItemRepository() {
        Request requestFirst = Request.builder().id(1L).build();
        Request requestSecond = Request.builder().id(2L).build();
        Request requestThird = Request.builder().id(3L).build();

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(requestRepository.findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong()))
                .thenReturn(List.of(requestFirst, requestSecond, requestThird));
        Mockito.when(itemRepository.findItemsByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        requestService.findRequestsByUserId(1L);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(3))
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void whenFindAllAnotherUsersRequestsThenReturnRequestsWithItemsDto() {
        Slice<Request> requests = new SliceImpl<>(List.of(request));

        Mockito.when(requestRepository.findAllOtherUsersRequests(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(requests);
        Mockito.when(itemRepository.findItemsByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        List<RequestDtoWithItems> returned = requestService.findAllAnotherUsersRequests(1L, 0, 10);

        Assertions.assertNotNull(returned.get(0).getItems());

        assertThat(returned.get(0).getItems(), equalTo(List.of(ItemMapper.toItemDto(item))));

        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllOtherUsersRequests(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void whenFindTwoAnotherUsersRequestsThenCallTwiceFindItemsItemRepository() {
        Request requestFirst = Request.builder().id(1L).build();
        Request requestSecond = Request.builder().id(2L).build();
        Slice<Request> requests = new SliceImpl<>(List.of(requestFirst, requestSecond));

        Mockito.when(requestRepository.findAllOtherUsersRequests(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(requests);
        Mockito.when(itemRepository.findItemsByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        requestService.findAllAnotherUsersRequests(1L, 0, 10);

        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllOtherUsersRequests(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(2))
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void whenTryToGetByIdByNotExistsUserThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.getById(1L, 5L));

        Assertions.assertEquals("Пользователь с id=1 не найден", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void whenTryToGetNotExistsRequestThenRequestNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(requestRepository.findById(5L))
                .thenThrow(new RequestNotFoundException("Request with id=5 не найден"));

        final RequestNotFoundException exception = Assertions.assertThrows(
                RequestNotFoundException.class,
                () -> requestService.getById(1L, 5L));

        Assertions.assertEquals("Request with id=5 не найден", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void whenGetRequestByIdThenReturnRequestWithItemsDto() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(requestRepository.findById(1L))
                .thenReturn(Optional.of(request));
        Mockito.when(itemRepository.findItemsByRequestId(1L))
                .thenReturn(List.of(item));

        RequestDtoWithItems returned = requestService.getById(1L, 1L);

        Assertions.assertNotNull(returned.getItems());

        assertThat(returned.getItems(), equalTo(List.of(ItemMapper.toItemDto(item))));

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequestId(Mockito.anyLong());
    }
}