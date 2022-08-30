package ru.practicum.shareit.user.repo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import ru.practicum.shareit.user.model.User;

import java.util.Optional;

@DataJpaTest
class TestUserRepository {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findById() {
        User user = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();

        em.persist(user);

        Optional<User> found = userRepository.findById(user.getId());

        Assertions.assertTrue(found.isPresent());
        assertThat(found.get().getName(), equalTo(user.getName()));
        assertThat(found.get().getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void existsUserByEmail() {
        User user = new User();
        user.setName("name");
        user.setEmail("email@mail.ru");

        em.persist(user);

        Boolean doesExist = userRepository.existsUserByEmail("email@mail.ru");
        assertThat(doesExist, equalTo(true));
    }
}