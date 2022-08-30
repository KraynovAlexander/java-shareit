package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.errorHandler.exception.RequestException;
import ru.practicum.shareit.errorHandler.exception.ItemException;
import ru.practicum.shareit.errorHandler.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.dto.CommentDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemDtoFull;
import ru.practicum.shareit.item.model.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utils.Constants;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class TestItemController {
    @Autowired
    private ItemController itemController;
    @MockBean
    private ItemService itemService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static ItemDto itemDto;
    private static ItemDto updated;
    private static CommentDto commentDto;

    @BeforeAll
    public static void beforeAll() {
        itemDto = ItemDto.builder()
                .id(1L)
                .ownerId(1L)
                .available(true)
                .name("book")
                .description("on java")
                .build();

        updated = ItemDto.builder()
                .available(false)
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .text("это был очень полезный предмет")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createValidItemStatusIsOk() throws Exception {
        Mockito
                .when(itemService.addNewItem(Mockito.anyLong(), Mockito.any(ItemDto.class)))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("ownerId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("on java"));
    }

    @Test
    void createItemByNotExistsUserStatusIsNotFound() throws Exception {
        Mockito
                .when(itemService.addNewItem(2L, itemDto))
                .thenThrow(new UserNotFoundException("Пользователь с id=2 не найден"));

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 2L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с id=2 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createItemWithBlankNameStatusIsBadRequest() throws Exception {
        ItemDto notValidName = ItemDto.builder()
                .name(" ")
                .description("good")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(notValidName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithBlankDescriptionStatusIsBadRequest() throws Exception {
        ItemDto emptyDescription = ItemDto.builder()
                .name("book")
                .description("")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(emptyDescription)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithAvailableNullStatusIsBadRequest() throws Exception {
        ItemDto availableNull = ItemDto.builder()
                .name("book")
                .description("good")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(availableNull)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postValidCommentStatusIsOk() throws Exception {
        Mockito
                .when(itemService.postComment(Mockito.any(CommentDto.class), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 2L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("text").value("это был очень полезный предмет"))
                .andExpect(MockMvcResultMatchers.jsonPath("created").exists());
    }

    @Test
    void postCommentIfBookingCurrentStatusIsBadRequest() throws Exception {
        Mockito
                .when(itemService.postComment(commentDto, 2L, 1L))
                .thenThrow(new RequestException("Пользователь с id=2 невозможно оставить комментарий к товару с id=1"));

        mockMvc.perform(post("/items/1/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 2L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RequestException))
                .andExpect(result -> assertEquals("Пользователь с id=2 невозможно оставить комментарий к товару с id=1",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void postEmptyCommentStatusIsBadRequest() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .text(" ")
                .authorName("author")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 2L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateValidItemStatusIsOk() throws Exception {
        ItemDto returned = ItemDto.builder()
                .id(1L)
                .available(false)
                .build();

        Mockito
                .when(itemService.updateItem(1L, 1L, updated))
                .thenReturn(returned);

        mockMvc.perform(patch("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("available").value(false));
    }

    @Test
    void updateItemNotOwnerStatusIsNotFound() throws Exception {
        Mockito
                .when(itemService.updateItem(2L, 1L, updated))
                .thenThrow(new UserNotFoundException("Пользователь с id=2 не найден"));

        mockMvc.perform(patch("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 2L)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с id=2 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNotExistsItemStatusIsNotFound() throws Exception {
        Mockito
                .when(itemService.updateItem(1L, 2L, updated))
                .thenThrow(new ItemException("Предмет с id=2 не найден"));

        mockMvc.perform(patch("/items/2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemException))
                .andExpect(result -> assertEquals("Предмет с id=2 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByItemIdIsOk() throws Exception {
        ItemDtoFull itemDtoFull = ItemDtoFull.builder()
                .id(1L).name("book")
                .description("on java")
                .ownerId(1L)
                .available(true)
                .build();

        Mockito
                .when(itemService.findItemById(1L, 1L))
                .thenReturn(itemDtoFull);

        mockMvc.perform(get("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("ownerId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("available").value(true));
    }

    @Test
    void getByItemIdNotExistsItemIsNotFound() throws Exception {
        Mockito
                .when(itemService.findItemById(2L, 2L))
                .thenThrow(new ItemException("Предмет с id=2 не найден"));

        mockMvc.perform(get("/items/2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemException))
                .andExpect(result -> assertEquals("Предмет с id=2 не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByUserIdStatusIsOk() throws Exception {
        ItemDtoWithBookings first = ItemDtoWithBookings.builder()
                .id(1L)
                .name("book")
                .description("on java")
                .build();
        ItemDtoWithBookings second = ItemDtoWithBookings.builder()
                .id(2L)
                .name("good item")
                .description("very good")
                .build();
        List<ItemDtoWithBookings> items = List.of(first, second);

        Mockito
                .when(itemService.getItemsByOwnerId(1L, 0, 10))
                .thenReturn(items);

        mockMvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Constants.SHARER, 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("good item"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("very good"));
    }

    @Test
    void searchStatusIsOk() throws Exception {
        ItemDto ownerItem = ItemDto.builder()
                .id(2L)
                .name("good item")
                .description("very good")
                .available(true)
                .build();

        Mockito
                .when(itemService.search("gOOd", 0, 10))
                .thenReturn(List.of(ownerItem));

        mockMvc.perform(get("/items/search?text=gOOd&from=0&size=10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("good item"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("very good"));
    }
}