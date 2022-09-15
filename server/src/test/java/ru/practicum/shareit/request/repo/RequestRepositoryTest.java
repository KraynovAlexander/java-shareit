package ru.practicum.shareit.request.repo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Slice;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Pagination;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class RequestRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private RequestRepository requestRepository;

    @Test
    void findAllByUserIdOrderByCreationTimeDesc() {
        User author = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();

        User another = User.builder()
                .name("another")
                .email("another@gmail.com")
                .build();

        Request one = Request.builder()
                .user(author)
                .description("one")
                .creationTime(LocalDateTime.now())
                .build();
        Request two = Request.builder()
                .user(another)
                .description("two")
                .creationTime(LocalDateTime.now())
                .build();
        Request three = Request.builder()
                .user(author)
                .description("three")
                .creationTime(LocalDateTime.now())
                .build();

        em.persist(author);
        em.persist(another);
        em.persist(one);
        em.persist(two);
        em.persist(three);

        List<Request> requests = requestRepository.findAllByUserIdOrderByCreationTimeDesc(author.getId());

        Assertions.assertEquals(2, requests.size());
        assertThat(requests.get(0), equalTo(three));
        assertThat(requests.get(1), equalTo(one));
    }

    @Test
    void findAllOtherUsersRequests() {
        User author = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();

        User another = User.builder()
                .name("another")
                .email("another@gmail.com")
                .build();

        Request one = Request.builder()
                .user(author)
                .description("one")
                .creationTime(LocalDateTime.now())
                .build();
        Request two = Request.builder()
                .user(another)
                .description("two")
                .creationTime(LocalDateTime.now())
                .build();
        Request three = Request.builder()
                .user(author)
                .description("three")
                .creationTime(LocalDateTime.now())
                .build();

        em.persist(author);
        em.persist(another);
        em.persist(one);
        em.persist(two);
        em.persist(three);

        Slice<Request> requests = requestRepository.findAllOtherUsersRequests(author.getId(), Pagination.of(0, 5));

        Assertions.assertEquals(1, requests.getContent().size());
        assertThat(requests.getContent().get(0), equalTo(two));
    }
}