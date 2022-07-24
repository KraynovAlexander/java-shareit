package ru.practicum.shareit.item;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessRightsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private long id;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto addNewItem(long userId, ItemDto item) {
        checkUser(userId);

        item.setOwnerId(userId);
        item.setId(++id);
        itemRepository.save(item);
        log.info("Предмет с id={} был успешно добавлен пользователем с id={}", item.getId(), userId);

        return item;
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto item) {
        checkUser(userId);

        ItemDto beingUpdated = itemRepository.getById(itemId);

        if (userId != beingUpdated.getOwnerId())
            throw new AccessRightsException(String.format("Пользователь с id=%s не имеет прав на обновление элемента с id=%s",
                    userId, itemId));

        if(item.getName() != null) beingUpdated.setName(item.getName());
        if(item.getDescription() != null) beingUpdated.setDescription(item.getDescription());
        if(item.getAvailable() != null) beingUpdated.setAvailable(item.getAvailable());
        itemRepository.update(beingUpdated);
        log.info("Предмет с id={} был успешно обновлен пользователем с id={}", itemId, userId);

        return beingUpdated;
    }

    @Override
    public ItemDto findItemById(long itemId) {
        return itemRepository.getById(itemId);
    }

    @Override
    public List<ItemDto> getItemsByOwnerId(long userId) {
        return itemRepository.findByUserId(userId);
    }

    @Override
    public List<ItemDto> search(String text) {                                     // поиск вещей
        if (text.isEmpty() || text.isBlank()) return new ArrayList<>();

        return itemRepository.search(text);
    }

    private void checkUser(long userId) {                                 // проверка есть ли пользователь в  базе
        if (!userRepository.doesUserExist(userId))
            throw new UserNotFoundException(String.format("Пользователь с id=%s не найден", userId));
    }
}

