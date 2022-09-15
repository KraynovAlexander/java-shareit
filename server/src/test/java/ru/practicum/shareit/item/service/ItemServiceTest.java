package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.mapper.BookingMapper;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.errorHandler.exceptions.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.CommentDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemDtoFull;
import ru.practicum.shareit.item.model.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.mapper.CommentMapper;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repo.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.utils.Pagination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private RequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;
    private static ItemDto itemDto;
    private static Item item;
    private static User user;
    private static CommentDto comment;

    @BeforeAll
    public static void beforeAll() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Book")
                .description("Thinking in Java")
                .available(true)
                .ownerId(1L)
                .build();

        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@ya.ru")
                .build();

        item = ItemMapper.toItem(itemDto, user);
        comment = CommentDto.builder().text("good").build();
    }

    @Test
    void whenAddItemByExistsUserThenCallSaveItemRepository() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(Mockito.any()))
                .thenReturn(item);

        ItemDto returned = itemService.addNewItem(1L, itemDto);

        assertThat(returned, equalTo(ItemMapper.toItemDto(item)));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(item);
    }

    @Test
    void whenAddItemByUserNotExistsThenUserNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenThrow(new UserNotFoundException("User with id=1 not found"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> itemService.addNewItem(1L, itemDto));

        Assertions.assertEquals("User with id=1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
    }

    @Test
    void whenAddItemWithRequestIdThenCallFindByIdRequestRepository() {
        Request request = Request.builder().id(2L).user(user).description("wants any java book").build();

        itemDto.setRequestId(2L);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(2L))
                .thenReturn(Optional.of(request));
        item.setRequest(request);
        Mockito.when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(item);

        ItemDto returned = itemService.addNewItem(1L, itemDto);

        assertThat(returned, equalTo(itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(Mockito.any(Item.class));
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(2L);
    }

    @Test
    void whenAddItemWithNotExistsRequestIdThenRequestNotFoundException() {
        itemDto.setRequestId(2L);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(2L))
                .thenThrow(new RequestNotFoundException("Request with id=2 not found"));

        final RequestNotFoundException exception = Assertions.assertThrows(
                RequestNotFoundException.class,
                () -> itemService.addNewItem(1L, itemDto));

        Assertions.assertEquals("Request with id=2 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.never())
                .save(item);
    }

    @Test
    void whenTryPostByNotExistsUserThenUserNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenThrow(new UserNotFoundException("User with id=1 not found"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> itemService.postComment(comment, 1L, 1L));

        Assertions.assertEquals("User with id=1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndBooker_IdOrderByEndAsc(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryPostCommentOnNotExistsItemThenItemNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenThrow(new ItemNotFoundException("Item with id=1 not found"));

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.postComment(comment, 1L, 1L));

        Assertions.assertEquals("Item with id=1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndBooker_IdOrderByEndAsc(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryPostCommentIfBookingDoesNotExistThenInvalidRequestException() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.getTopByItem_IdAndBooker_IdOrderByEndAsc(1L, 1L))
                .thenReturn(Optional.empty());

        final InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> itemService.postComment(comment, 1L, 1L));

        Assertions.assertEquals("The user with id=1 cannot leave a comment on the item with id=1",
                exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndBooker_IdOrderByEndAsc(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryPostCommentIfBookingCurrentThenInvalidRequestException() {
        Booking currentBooking = Booking.builder().end(LocalDateTime.now().plusDays(1L)).build();

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.getTopByItem_IdAndBooker_IdOrderByEndAsc(1L, 1L))
                .thenReturn(Optional.of(currentBooking));

        final InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> itemService.postComment(comment, 1L, 1L));

        Assertions.assertEquals("The user with id=1 cannot leave a comment on the item with id=1",
                exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndBooker_IdOrderByEndAsc(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryPostValidCommentThenSaveCommentRepository() {
        Booking pastBooking = Booking.builder().end(LocalDateTime.now().minusDays(1L)).build();
        Comment validComment = CommentMapper.toComment(comment, item, user);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.getTopByItem_IdAndBooker_IdOrderByEndAsc(1L, 1L))
                .thenReturn(Optional.of(pastBooking));
        Mockito.when(commentRepository.save(Mockito.any()))
                .thenReturn(validComment);

        CommentDto commentDto = itemService.postComment(comment, 1L, 1L);

        assertThat(commentDto, equalTo(CommentMapper.toCommentDto(validComment)));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndBooker_IdOrderByEndAsc(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    void whenUpdateItemByNotExistsUserThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> itemService.updateItem(1L, 1L, itemDto));

        Assertions.assertEquals("User with id=1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenUpdateItemNotExistsThenItemNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(itemRepository.findById(1L))
                .thenThrow(new ItemNotFoundException("Item with id=1 not found"));

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.updateItem(1L, 1L, itemDto));

        Assertions.assertEquals("Item with id=1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenUpdateItemByUserNotOwnerThenNoAccessRightsException() {
        User anotherOwner = User.builder().id(5L).build();
        Item itemWithAnotherOwner = Item.builder().id(1L).owner(anotherOwner).build();

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(itemWithAnotherOwner));

        final NoAccessRightsException exception = Assertions.assertThrows(
                NoAccessRightsException.class,
                () -> itemService.updateItem(1L, 1L, itemDto));

        Assertions.assertEquals("User with id=1 has no rights to update item with id=1", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void whenTryFindNotExistsItemByIdThenItemNotFoundException() {
        Mockito.when(itemRepository.findById(1L))
                .thenThrow(new ItemNotFoundException("Item with id=1 not found"));

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.findItemById(1L, 1L));

        Assertions.assertEquals("Item with id=1 not found", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never())
                .findCommentsByItem_Id(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void whenFindItemByIdUserIsOwnerThenReturnItemWithBookingsDate() {
        List<Comment> comments = List.of(CommentMapper.toComment(comment, item, user));
        Booking last = Booking.builder().item(item).booker(user).start(LocalDateTime.now().minusDays(1)).build();
        Booking next = Booking.builder().item(item).booker(user).start(LocalDateTime.now().plusDays(1)).build();
        User owner = User.builder().id(1L).build();
        item.setOwner(owner);

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findCommentsByItem_Id(1L))
                .thenReturn(comments);
        Mockito.when(bookingRepository.getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(last));
        Mockito.when(bookingRepository.getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(next));

        ItemDtoFull returned = itemService.findItemById(1L, 1L);

        Assertions.assertNotNull(returned.getLastBooking());
        Assertions.assertNotNull(returned.getNextBooking());

        BookingShortDto lastShortDto = BookingMapper.toBookingShortDto(last);
        BookingShortDto nextShortDto = BookingMapper.toBookingShortDto(next);
        CommentDto returnedComment = CommentMapper.toCommentDto(comments.get(0));

        assertThat(returned, equalTo(ItemMapper.toItemDtoFull(item, lastShortDto, nextShortDto, List.of(returnedComment))));

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.times(1))
                .findCommentsByItem_Id(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void whenFindItemByIdUserIsNotOwnerThenReturnItemBookingsDateNull() {
        List<Comment> comments = List.of(CommentMapper.toComment(comment, item, user));

        User owner = User.builder().id(5L).build();
        item.setOwner(owner);

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findCommentsByItem_Id(1L))
                .thenReturn(comments);

        ItemDtoFull returned = itemService.findItemById(1L, 1L);

        Assertions.assertNull(returned.getNextBooking());
        Assertions.assertNull(returned.getLastBooking());

        CommentDto returnedComment = CommentMapper.toCommentDto(comments.get(0));

        assertThat(returned, equalTo(ItemMapper.toItemDtoFull(item, null, null, List.of(returnedComment))));

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.times(1))
                .findCommentsByItem_Id(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void whenGetItemsByOwnerIdThenReturnItemWithBookingsDate() {
        Slice<Item> items = new SliceImpl<>(List.of(item));
        Booking last = Booking.builder().item(item).booker(user).start(LocalDateTime.now().minusDays(1)).build();
        Booking next = Booking.builder().item(item).booker(user).start(LocalDateTime.now().plusDays(1)).build();

        Mockito.when(itemRepository.findItemsByOwnerId(Mockito.anyLong(), Mockito.any(Pagination.class)))
                .thenReturn(items);
        Mockito.when(bookingRepository.getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(last));
        Mockito.when(bookingRepository.getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(next));

        List<ItemDtoWithBookings> returnedItems = itemService.getItemsByOwnerId(1L, 0, 10);

        Assertions.assertNotNull(returnedItems.get(0).getLastBooking());
        Assertions.assertNotNull(returnedItems.get(0).getNextBooking());

        BookingShortDto lastShortDto = BookingMapper.toBookingShortDto(last);
        BookingShortDto nextShortDto = BookingMapper.toBookingShortDto(next);

        assertThat(returnedItems.get(0), equalTo(ItemMapper.toItemDtoWithBookings(item, lastShortDto, nextShortDto)));

        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByOwnerId(1L, Pagination.of(0, 10, Sort.by("id").ascending()));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void whenSearchValidTextThenCallSearchItemRepository() {
        Slice<Item> items = new SliceImpl<>(List.of(item));

        Mockito.when(itemRepository.search(Mockito.anyString(), Mockito.any(Pageable.class)))
                .thenReturn(items);

        List<ItemDto> returned = itemService.search("book", 0, 10);

        assertThat(returned.size(), equalTo(1));

        Mockito.verify(itemRepository, Mockito.times(1))
                .search(Mockito.anyString(), Mockito.any(Pageable.class));
    }
}