package ru.practicum.shareit.request.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.practicum.shareit.errorHandler.ErrorHandler;

import ru.practicum.shareit.errorHandler.exception.RequestNotFoundException;
import ru.practicum.shareit.errorHandler.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.dto.ItemDto;

import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestDtoWithItems;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.utils.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc
class TestRequestController {
    @Autowired
    private RequestController requestController;
    @MockBean
    private RequestService requestService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static UserDto userDto;
    private static RequestInDto requestInDto;
    private static RequestDto requestDto;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .email("user@yandex.ru")
                .name("userName")
                .build();

        requestInDto = RequestInDto.builder()
                .description("I need book on java")
                .build();

        requestDto = RequestDto.builder()
                .id(1L)
                .userId(1L)
                .description("I need book on java")
                .created(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createValidRequestStatusIsOk() throws Exception {
        Mockito
                .when(requestService.addNewRequest(1L, requestInDto))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("userId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("I need book on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("created").exists());
    }

    @Test
    void createRequestByNotExistsUserStatusIsNotFound() throws Exception {
        Mockito
                .when(requestService.addNewRequest(1L, requestInDto))
                .thenThrow(new UserNotFoundException("Пользователь с id=1 не найден"));

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с id=1 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void createRequestWithBlankDescriptionStatusIsBadRequest() throws Exception {
        RequestInDto blankDescription = RequestInDto.builder()
                .description(" ")
                .build();

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(blankDescription)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getAllByUserIdStatusIsOk() throws Exception {
        RequestDtoWithItems first = RequestDtoWithItems.builder()
                .id(2L)
                .description("second")
                .items(List.of(ItemDto.builder()
                        .id(1L)
                        .ownerId(2L)
                        .requestId(2L)
                        .available(true)
                        .name("two")
                        .description("very useful")
                        .build()))
                .build();
        RequestDtoWithItems second = RequestDtoWithItems.builder()
                .id(1L)
                .description("first")
                .build();
        List<RequestDtoWithItems> requests = List.of(first, second);

        Mockito
                .when(requestService.findRequestsByUserId(1L))
                .thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("second"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].ownerId").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].name").value("two"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].description").value("very useful"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("first"));
    }

    @Test
    void getAllByUserIdByNotExistsUserStatusIsNotFound() throws Exception {
        Mockito
                .when(requestService.findRequestsByUserId(3L))
                .thenThrow(new UserNotFoundException("Пользователь с id=3 не найден"));

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 3L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с id=3 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getAllAnotherUsersRequestsStatusIsOk() throws Exception {
        RequestDtoWithItems first = RequestDtoWithItems.builder()
                .id(2L)
                .description("second")
                .build();
        RequestDtoWithItems second = RequestDtoWithItems.builder()
                .id(1L)
                .description("first")
                .build();
        List<RequestDtoWithItems> requests = List.of(first, second);

        Mockito
                .when(requestService.findAllAnotherUsersRequests(2L, 0, 5))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all?from=0&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 2L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("second"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("first"));
    }

    @Test
    void getByRequestIdStatusIsOk() throws Exception {
        RequestDtoWithItems requestDtoWithItems = RequestDtoWithItems.builder()
                .id(1L)
                .description("I need book on java")
                .build();

        Mockito
                .when(requestService.getById(1L, 1L))
                .thenReturn(requestDtoWithItems);

        mockMvc.perform(get("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("I need book on java"));
    }

    @Test
    void getByRequestIdByNotExistsUserStatusIsNotFound() throws Exception {
        Mockito
                .when(requestService.getById(1L, 1L))
                .thenThrow(new UserNotFoundException("Пользователь с id=1 не найден"));

        mockMvc.perform(get("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с id=1 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getByNotExistsRequestIdStatusIsNotFound() throws Exception {
        Mockito
                .when(requestService.getById(1L, 5L))
                .thenThrow(new RequestNotFoundException("Запрос с id=5 не найден"));

        mockMvc.perform(get("/requests/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RequestNotFoundException))
                .andExpect(result -> assertEquals("Запрос с id=5 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }
}