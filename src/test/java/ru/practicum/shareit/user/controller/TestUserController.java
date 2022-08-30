package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.practicum.shareit.errorHandler.ErrorHandler;
import ru.practicum.shareit.errorHandler.exception.UserException;
import ru.practicum.shareit.errorHandler.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class TestUserController {
    @Autowired
    private UserController userController;
    @MockBean
    private UserService userService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static UserDto userDto;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .id(1L)
                .email("user@yandex.ru")
                .name("userName")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void createValidUserStatusIsOk() throws Exception {
        Mockito
                .when(userService.save(Mockito.any(UserDto.class)))
                .thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("userName"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("user@yandex.ru"));
    }

    @Test
    void createUserWithInvalidEmailStatusIsBadRequest() throws Exception {
        UserDto invalidEmail = UserDto.builder()
                .name("User")
                .email("us")
                .build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateValidUserStatusIsOk() throws Exception {
        UserDto updated = UserDto.builder()
                .id(1L)
                .name("New")
                .email("new@ya.ru")
                .build();

        Mockito
                .when(userService.update(1L, userDto))
                .thenReturn(updated);

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("New"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("new@ya.ru"));
    }

    @Test
    void updateUserDuplicateEmailStatusIsConflict() throws Exception {
        Mockito
                .when(userService.update(2L, userDto))
                .thenThrow(new UserException("Пользователь с электронной почтой=new@ya.ru already exists"));

        mockMvc.perform(patch("/users/2")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException))
                .andExpect(result -> assertEquals("Пользователь с электронной почтой=new@ya.ru already exists",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isConflict());
    }

    @Test
    void updateNotExistsUserStatusIsNotFound() throws Exception {
        Mockito
                .when(userService.update(10L, userDto))
                .thenThrow(new UserNotFoundException("Пользователь с id=10 не найден"));

        mockMvc.perform(patch("/users/10")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с id=10 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findValidUserByIdStatusIsOk() throws Exception {
        Mockito
                .when(userService.findById(1L))
                .thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("userName"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("user@yandex.ru"));
    }

    @Test
    void findNotExistsUserByIdStatusIsNotFound() throws Exception {
        Mockito
                .when(userService.findById(1L))
                .thenThrow(new UserNotFoundException("Пользователь с id=1 не найден"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/1"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с id=1 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllStatusIsOk() throws Exception {
        UserDto secondUserDto = UserDto.builder()
                .id(2L)
                .email("second@yandex.ru")
                .name("two")
                .build();

        Mockito
                .when(userService.findAll())
                .thenReturn(List.of(userDto, secondUserDto));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("userName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("user@yandex.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("two"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("second@yandex.ru"));
    }

    @Test
    void deleteValidUserByIdStatusIsOk() throws Exception {
        mockMvc.perform(delete("/users/1")
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }
}