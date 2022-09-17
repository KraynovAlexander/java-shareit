package ru.practicum.shareit.item;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {
    @Autowired
    private ItemController itemController;
    @MockBean
    private ItemClient itemClient;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static ItemDto itemDto;
    private static CommentDto commentDto;
    private static ResponseEntity<Object> responseIsOk;

    @BeforeAll
    public static void beforeAll() {
        itemDto = ItemDto.builder()
                .available(true)
                .name("book")
                .description("on java")
                .build();

        commentDto = CommentDto.builder()
                .authorName("Nikki")
                .text("like it")
                .build();

        responseIsOk = ResponseEntity
                .status(HttpStatus.OK)
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
    void whenCreateValidItemThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.create(2L, itemDto))
                .thenReturn(responseIsOk);

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .create(2L, itemDto);
    }

    @Test
    void whenCreateItemIfNameIsNullThenStatusIsBadRequest() throws Exception {
        ItemDto withoutName = ItemDto.builder()
                .available(true)
                .description("on java")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withoutName)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withoutName);
    }

    @Test
    void whenCreateItemWithBlankNameThenStatusIsBadRequest() throws Exception {
        ItemDto withBlankName = ItemDto.builder()
                .name("")
                .available(true)
                .description("on java")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withBlankName)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withBlankName);
    }

    @Test
    void whenCreateItemIfDescriptionIsNullThenStatusIsBadRequest() throws Exception {
        ItemDto withoutDescription = ItemDto.builder()
                .name("Item")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withoutDescription)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withoutDescription);
    }

    @Test
    void whenCreateItemWithBlankDescriptionThenStatusIsBadRequest() throws Exception {
        ItemDto withBlankDescription = ItemDto.builder()
                .name("Item")
                .available(true)
                .description("\n")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withBlankDescription)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withBlankDescription);
    }

    @Test
    void whenCreateItemIfAvailableIsNullThenStatusIsBadRequest() throws Exception {
        ItemDto withoutAvailable = ItemDto.builder()
                .name("Item")
                .description("very useful")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withoutAvailable)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withoutAvailable);
    }

    @Test
    void whenPostValidCommentThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.postComment(1L, commentDto, 3L))
                .thenReturn(responseIsOk);

        mockMvc.perform(post("/items/3/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .postComment(1L, commentDto, 3L);
    }

    @Test
    void whenPostCommentIfTextIsNullThenStatusIsBadRequest() throws Exception {
        CommentDto commentWithoutText = CommentDto.builder()
                .authorName("Nikki")
                .build();

        mockMvc.perform(post("/items/3/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentWithoutText)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .postComment(1L, commentWithoutText, 3L);
    }

    @Test
    void whenPostCommentWithBlankTextThenStatusIsBadRequest() throws Exception {
        CommentDto commentWithBlankText = CommentDto.builder()
                .authorName("Nikki")
                .text("\t")
                .build();

        mockMvc.perform(post("/items/3/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentWithBlankText)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .postComment(1L, commentWithBlankText, 3L);
    }

    @Test
    void whenPostCommentAndNegativeItemIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/items/-3/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .postComment(1L, commentDto, -3L);
    }

    @Test
    void whenUpdateValidItemThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.update(1L, itemDto, 2L))
                .thenReturn(responseIsOk);

        mockMvc.perform(patch("/items/2")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .update(1L, itemDto, 2L);
    }

    @Test
    void whenUpdateItemIfNegativeItemIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/items/-2")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .update(1L, itemDto, -2L);
    }

    @Test
    void whenGetValidItemByIdThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.getByItemId(1L, 2L))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/items/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .getByItemId(1L, 2L);
    }

    @Test
    void whenTryToGetItemByNegativeIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items/-2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .getByItemId(1L, -2L);
    }

    @Test
    void whenGetValidItemByUserIdThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.getByUserId(5L, 3, 2))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/items?from=3&size=2")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .getByUserId(5L, 3, 2);
    }

    @Test
    void whenTryToGetItemByUserIdAndNegativeFromThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items?from=-3&size=2")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .getByUserId(5L, -3, 2);
    }

    @Test
    void whenTryToGetItemByUserIdAndSizeLessThanOneThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items?from=3&size=0")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .getByUserId(5L, -3, 0);
    }

    @Test
    void whenSearchThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.search("text", 0, 10))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/items/search?text=text"))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .search("text", 0, 10);
    }

    @Test
    void whenSearchIfBlankTextThenStatusIsOkAndReturnEmptyList() throws Exception {
        mockMvc.perform(get("/items/search?text="))
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .search("", 0, 10);
    }

    @Test
    void whenTryToSearchAndNegativeFromThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search?text=text&from=-3&size=3"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .search("text", -3, 3);
    }

    @Test
    void whenTryToSearchAndSizeLessThanOneThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search?text=text&from=3&size=0"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .search("text", 3, 0);
    }
}