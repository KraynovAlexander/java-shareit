package ru.practicum.shareit.request.service;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.item.service.ItemService;

import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class TestRequestServiceImpl {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final RequestService requestService;
    private static UserDto userDto;
    private static RequestInDto requestInDto;

    @BeforeAll
    public static void setUp() {
        userDto = UserDto.builder()
                .email("user@yandex.ru")
                .name("userName")
                .build();

        requestInDto = RequestInDto.builder()
                .description("I need book")
                .build();
    }

    @Test
    void addNewRequest() {
        UserDto savedUser = userService.save(userDto);

        RequestDto savedRequest = requestService.addNewRequest(savedUser.getId(), requestInDto);

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.id = :requestId", Request.class);
        Request request = query
                .setParameter("requestId", savedRequest.getId())
                .getSingleResult();

        assertThat(savedRequest.getId(), notNullValue());
        assertThat(savedRequest.getCreated(), notNullValue());
        assertThat(savedRequest.getDescription(), equalTo(request.getDescription()));
        assertThat(savedRequest.getUserId(), equalTo(savedUser.getId()));
    }

    @Test
    void findRequestsByUserId() {
        UserDto savedUser = userService.save(userDto);
        RequestDto savedFirstRequest = requestService.addNewRequest(savedUser.getId(), requestInDto);
        RequestInDto second = RequestInDto.builder()
                .description("I need patience")
                .build();
        RequestDto savedSecondRequest = requestService.addNewRequest(savedUser.getId(), second);

        UserDto anotherUser = UserDto.builder()
                .name("another")
                .email("another@gmail.com")
                .build();
        RequestInDto anotherRequest = RequestInDto.builder()
                .description("I need something")
                .build();
        UserDto returnedAnotherUser = userService.save(anotherUser);
        requestService.addNewRequest(returnedAnotherUser.getId(), anotherRequest);

        List<RequestDtoWithItems> userRequests = requestService.findRequestsByUserId(savedUser.getId());

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.user.id = :userId", Request.class);
        List<Request> requests = query
                .setParameter("userId", savedUser.getId())
                .getResultList();

        assertThat(requests.size(), equalTo(2));
        assertThat(requests.get(0).getId(), equalTo(requests.get(0).getId()));
        assertThat(requests.get(0).getUser(), equalTo(UserMapper.toUser(savedUser)));
        assertThat(requests.get(0).getDescription(), equalTo(savedFirstRequest.getDescription()));
        assertThat(requests.get(1).getId(), equalTo(requests.get(1).getId()));
        assertThat(requests.get(1).getUser(), equalTo(UserMapper.toUser(savedUser)));
        assertThat(requests.get(1).getDescription(), equalTo(savedSecondRequest.getDescription()));
    }

    @Test
    void findAllAnotherUsersRequests() {
        UserDto savedUser = userService.save(userDto);
        RequestDto savedFirstRequest = requestService.addNewRequest(savedUser.getId(), requestInDto);
        RequestInDto second = RequestInDto.builder()
                .description("I need patience")
                .build();
        RequestDto savedSecondRequest = requestService.addNewRequest(savedUser.getId(), second);

        UserDto anotherUser = UserDto.builder()
                .name("another")
                .email("another@gmail.com")
                .build();
        RequestInDto anotherRequest = RequestInDto.builder()
                .description("I need something")
                .build();
        UserDto returnedAnotherUser = userService.save(anotherUser);
        requestService.addNewRequest(returnedAnotherUser.getId(), anotherRequest);

        List<RequestDtoWithItems> anotherUserRequests = requestService
                .findAllAnotherUsersRequests(returnedAnotherUser.getId(), 0, 10);

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.user.id <> :userId", Request.class);
        List<Request> requests = query
                .setParameter("userId", returnedAnotherUser.getId())
                .getResultList();

        assertThat(requests.size(), equalTo(2));
        assertThat(requests.get(0).getId(), equalTo(requests.get(0).getId()));
        assertThat(requests.get(0).getUser(), equalTo(UserMapper.toUser(savedUser)));
        assertThat(requests.get(0).getDescription(), equalTo(savedFirstRequest.getDescription()));
        assertThat(requests.get(1).getId(), equalTo(requests.get(1).getId()));
        assertThat(requests.get(1).getUser(), equalTo(UserMapper.toUser(savedUser)));
        assertThat(requests.get(1).getDescription(), equalTo(savedSecondRequest.getDescription()));
    }

    @Test
    void getById() {
        UserDto savedUser = userService.save(userDto);

        RequestDto savedRequest = requestService.addNewRequest(savedUser.getId(), requestInDto);

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.id = :requestId", Request.class);
        Request request = query
                .setParameter("requestId", savedRequest.getId())
                .getSingleResult();

        assertThat(savedRequest.getId(), equalTo(request.getId()));
        assertThat(savedRequest.getCreated(), equalTo(request.getCreationTime()));
        assertThat(savedRequest.getDescription(), equalTo(request.getDescription()));
        assertThat(savedRequest.getUserId(), equalTo(request.getUser().getId()));
    }
}