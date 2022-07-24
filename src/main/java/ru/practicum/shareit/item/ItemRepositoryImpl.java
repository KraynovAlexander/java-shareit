package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, ItemDto> items = new HashMap<>();

    @Override
    public void save(ItemDto item) {
        items.put(item.getId(), item);
    }

    @Override
    public void update(ItemDto item) {
        items.put(item.getId(), item);
    }

    @Override
    public ItemDto getById(long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<ItemDto> findByUserId(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.trim().toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.trim().toLowerCase()))
                .filter(ItemDto::getAvailable)
                .collect(Collectors.toList());
    }
}