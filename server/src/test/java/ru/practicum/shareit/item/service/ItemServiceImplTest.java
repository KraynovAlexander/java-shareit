package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.CommentDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemDtoFull;
import ru.practicum.shareit.item.model.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private static UserDto userDto;
    private static ItemDto itemDto;
    private static CommentDto commentDto;
    private static BookingInDto bookingInDto;

    @BeforeAll
    public static void setUp() {
        userDto = UserDto.builder()
                .email("user@yandex.ru")
                .name("userName")
                .build();

        itemDto = ItemDto.builder()
                .name("book")
                .description("JAVA reference")
                .available(true)
                .build();

        commentDto = CommentDto.builder()
                .text("good")
                .build();

        bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1L))
                .end(LocalDateTime.now().plusSeconds(2L))
                .build();
    }

    @Test
    void addNewItem() {
        UserDto savedUser = userService.save(userDto);

        ItemDto saved = itemService.addNewItem(savedUser.getId(), itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :savedId", Item.class);
        Item item = query
                .setParameter("savedId", saved.getId())
                .getSingleResult();

        assertThat(saved.getId(), notNullValue());
        assertThat(item.getId(), equalTo(saved.getId()));
        assertThat(item.getOwner(), equalTo(UserMapper.toUser(savedUser)));
        assertThat(item.getName(), equalTo(saved.getName()));
        assertThat(item.getDescription(), equalTo(saved.getDescription()));
    }

    @Test
    void postComment() throws InterruptedException {
        UserDto another = UserDto.builder()
                .name("anotherUser")
                .email("another@ya.ru")
                .build();
        UserDto booker = userService.save(another);
        UserDto itemOwner = userService.save(userDto);
        ItemDto savedItem = itemService.addNewItem(itemOwner.getId(), itemDto);
        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(savedItem.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1L))
                .end(LocalDateTime.now().plusSeconds(2L))
                .build();
        bookingInDto.setItemId(savedItem.getId());
        bookingInDto.setBookerId(booker.getId());
        bookingService.addNewBooking(booker.getId(), bookingInDto);
        Thread.sleep(2000);

        CommentDto saved = itemService.postComment(commentDto, booker.getId(), savedItem.getId());

        TypedQuery<Comment> query = em.createQuery("Select c from Comment c where c.id = :savedId", Comment.class);
        Comment comment = query
                .setParameter("savedId", saved.getId())
                .getSingleResult();

        assertThat(saved.getId(), notNullValue());
        assertThat(comment.getId(), equalTo(saved.getId()));
        assertThat(comment.getAuthor(), equalTo(UserMapper.toUser(booker)));
        assertThat(comment.getItem(), equalTo(ItemMapper.toItem(savedItem, UserMapper.toUser(itemOwner))));
        assertThat(comment.getText(), equalTo(saved.getText()));
        assertThat(comment.getCreated(), notNullValue());
    }

    @Test
    void updateItem() {
        UserDto itemOwner = userService.save(userDto);
        ItemDto old = itemService.addNewItem(itemOwner.getId(), itemDto);
        ItemDto updated = ItemDto.builder()
                .name("newBook")
                .available(false)
                .build();

        ItemDto updatedAndReturned = itemService.updateItem(itemOwner.getId(), old.getId(), updated);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :savedId", Item.class);
        Item item = query
                .setParameter("savedId", old.getId())
                .getSingleResult();

        System.out.println(item);

        assertThat(item.getId(), equalTo(updatedAndReturned.getId()));
        assertThat(item.getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(item.getName(), equalTo(updated.getName()));
        assertThat(item.getAvailable(), equalTo(updated.getAvailable()));
        assertThat(item.getDescription(), equalTo(old.getDescription()));
    }

    @Test
    void findItemById() {
        UserDto itemOwner = userService.save(userDto);
        ItemDto saved = itemService.addNewItem(itemOwner.getId(), itemDto);

        ItemDtoFull foundById = itemService.findItemById(itemOwner.getId(), saved.getId());

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :savedId", Item.class);
        Item item = query
                .setParameter("savedId", saved.getId())
                .getSingleResult();

        assertThat(foundById.getComments(), notNullValue());
        assertThat(item.getName(), equalTo(foundById.getName()));
        assertThat(item.getDescription(), equalTo(foundById.getDescription()));
    }

    @Test
    void getItemsByOwnerId() {
        UserDto itemOneOwner = userService.save(userDto);
        itemService.addNewItem(itemOneOwner.getId(), itemDto);
        UserDto anotherOwner = UserDto.builder()
                .email("anotherOwner@ya.ru")
                .name("anotherOwner")
                .build();
        UserDto itemTwoAndThreeOwner = userService.save(anotherOwner);
        ItemDto two = ItemDto.builder()
                .name("two")
                .description("first item of another owner")
                .available(true)
                .build();
        ItemDto three = ItemDto.builder()
                .name("three")
                .description("second item of another owner")
                .available(true)
                .build();
        ItemDto savedTwo = itemService.addNewItem(itemTwoAndThreeOwner.getId(), two);
        ItemDto savedThree = itemService.addNewItem(itemTwoAndThreeOwner.getId(), three);

        List<ItemDtoWithBookings> items = itemService.getItemsByOwnerId(itemTwoAndThreeOwner.getId(), 0, 10);

        assertThat(items.size(), equalTo(2));
        assertThat(items.get(0).getOwnerId(), equalTo(itemTwoAndThreeOwner.getId()));
        assertThat(items.get(1).getOwnerId(), equalTo(itemTwoAndThreeOwner.getId()));
        assertThat(items.get(0).getId(), equalTo(savedTwo.getId()));
        assertThat(items.get(1).getId(), equalTo(savedThree.getId()));
    }

    @Test
    void search() {
        UserDto itemOneOwner = userService.save(userDto);
        itemService.addNewItem(itemOneOwner.getId(), itemDto);
        UserDto another = UserDto.builder()
                .email("another@ya.ru")
                .name("another")
                .build();
        UserDto anotherOwner = userService.save(another);
        ItemDto two = ItemDto.builder()
                .name("book thinking on java")
                .description("very useful book")
                .available(true)
                .build();
        ItemDto three = ItemDto.builder()
                .name("book")
                .description("reference")
                .available(true)
                .build();
        ItemDto savedTwo = itemService.addNewItem(anotherOwner.getId(), two);
        itemService.addNewItem(anotherOwner.getId(), three);

        List<ItemDto> found = itemService.search("java", 0, 10);

        assertThat(found.size(), equalTo(2));
        assertThat(found.get(0).getName(), equalTo(itemDto.getName()));
        assertThat(found.get(0).getDescription(), equalTo(itemDto.getDescription()));
        assertThat(found.get(1).getName(), equalTo(savedTwo.getName()));
        assertThat(found.get(1).getDescription(), equalTo(savedTwo.getDescription()));
    }
}