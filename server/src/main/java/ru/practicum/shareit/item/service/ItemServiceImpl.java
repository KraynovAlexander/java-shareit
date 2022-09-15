package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           RequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public ItemDto addNewItem(long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto, userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", userId))));

        if (itemDto.getRequestId() != null) {
            Request request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new RequestNotFoundException(String.format("Request with id=%s not found",
                            itemDto.getRequestId())));
            item.setRequest(request);
        }

        itemRepository.save(item);
        log.info("Item with id={} has successfully added by user with id={}", item.getId(), userId);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public CommentDto postComment(CommentDto commentDto, long userId, long itemId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", userId)));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Item with id=%s not found", itemId)));
        Optional<Booking> bookingOptional = bookingRepository.getTopByItem_IdAndBooker_IdOrderByEndAsc(itemId, userId);

        if (bookingOptional.isEmpty() || bookingOptional.get().getEnd().isAfter(LocalDateTime.now()))
            throw new InvalidRequestException(String.format("The user with id=%s cannot leave a comment on the item " +
                    "with id=%s", userId, itemId));

        commentDto.setCreated(LocalDateTime.now());
        Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, item, author));
        log.info("the user id={} has successfully create a comment on the item id={}", userId, itemId);

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        checkUser(userId);

        Item beingUpdated = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Item with id=%s not found", itemId)));

        if (userId != beingUpdated.getOwner().getId())
            throw new NoAccessRightsException(String.format("User with id=%s has no rights to update item with id=%s",
                    userId, itemId));

        if (itemDto.getName() != null) beingUpdated.setName(itemDto.getName());

        if (itemDto.getDescription() != null) beingUpdated.setDescription(itemDto.getDescription());

        if (itemDto.getAvailable() != null) beingUpdated.setAvailable(itemDto.getAvailable());

        itemRepository.save(beingUpdated);
        log.info("Item with id={} has successfully updated by user with id={}", itemId, userId);

        return ItemMapper.toItemDto(beingUpdated);
    }

    @Override
    public ItemDtoFull findItemById(long userId, long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Item with id=%s not found", itemId)));

        List<CommentDto> comments = commentRepository.findCommentsByItem_Id(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return item.getOwner().getId() == userId ? ItemMapper.toItemDtoFull(item, getLastBooking(itemId),
                getNextBooking(itemId), comments) : ItemMapper.toItemDtoFull(item, null, null, comments);
    }

    @Override
    public List<ItemDtoWithBookings> getItemsByOwnerId(long userId, int from, int size) {
        Pageable pageable = Pagination.of(from, size, Sort.by("id").ascending());

        return itemRepository.findItemsByOwnerId(userId, pageable).get()
                .map(item -> ItemMapper.toItemDtoWithBookings(item, getLastBooking(item.getId()),
                        getNextBooking(item.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text, int from, int size) {  // поиск вещей по содержанию введенного текста в имени или описании
        Pageable pageable = Pagination.of(from, size);

        return itemRepository.search(text, pageable).get()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkUser(long userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(String.format("User with id=%s not found", userId));
    }

    private BookingShortDto getLastBooking(long itemId) {
        Optional<Booking> lastBookingOptional = bookingRepository.getTopByItem_IdAndEndBeforeOrderByStartDesc(itemId,
                LocalDateTime.now());

        return lastBookingOptional.isEmpty() ? null : BookingMapper.toBookingShortDto(lastBookingOptional.get());
    }

    private BookingShortDto getNextBooking(long itemId) {
        Optional<Booking> nextBookingOptional = bookingRepository.getTopByItem_IdAndStartAfterOrderByStartDesc(itemId,
                LocalDateTime.now());

        return nextBookingOptional.isEmpty() ? null : BookingMapper.toBookingShortDto(nextBookingOptional.get());
    }
}
