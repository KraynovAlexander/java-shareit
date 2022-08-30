package ru.practicum.shareit.item.repo;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Slice;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Pagination;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class TestItemRepository {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findItemsByOwnerId() {
        User user = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(user)
                .available(true)
                .description("good")
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(user)
                .available(true)
                .description("another good")
                .build();

        em.persist(user);
        em.persist(first);
        em.persist(second);

        Slice<Item> items = itemRepository.findItemsByOwnerId(user.getId(), Pagination.of(0, 5));

        Assertions.assertEquals(2, items.getContent().size());
        assertThat(items.getContent().get(0), equalTo(first));
        assertThat(items.getContent().get(1), equalTo(second));
    }

    @Test
    void existsByOwnerId() {
        User user = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(user)
                .available(true)
                .description("good")
                .build();

        em.persist(user);
        em.persist(first);

        boolean exists = itemRepository.existsByOwnerId(user.getId());

        Assertions.assertTrue(exists);
    }

    @Test
    void findItemsByRequestId() {
        User user = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        Request request = Request.builder()
                .user(user)
                .creationTime(LocalDateTime.now())
                .description("need")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(user)
                .available(true)
                .description("good")
                .request(request)
                .build();
        Item second = Item.builder()
                .name("second")
                .owner(user)
                .available(true)
                .description("another good")
                .request(request)
                .build();

        em.persist(user);
        em.persist(request);
        em.persist(first);
        em.persist(second);

        List<Item> items = itemRepository.findItemsByRequestId(request.getId());

        Assertions.assertEquals(2, items.size());
        assertThat(items.get(0), equalTo(first));
        assertThat(items.get(1), equalTo(second));
    }
}