package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {
    private final EntityManager em;
    private final UserService userService;
    private static UserDto userDto;

    @BeforeAll
    public static void setUp() {
        userDto = UserDto.builder()
                .email("user@yandex.ru")
                .name("userName")
                .build();
    }

    @Test
    void save() {
        UserDto saved = userService.save(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query
                .setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(saved.getId(), notNullValue());
        assertThat(user.getId(), equalTo(saved.getId()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void update() {
        UserDto saved = userService.save(userDto);

        UserDto updated = UserDto.builder()
                .name("NewName")
                .email("new@gmail.com")
                .build();

        userService.update(saved.getId(), updated);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :initId", User.class);
        User user = query
                .setParameter("initId", saved.getId())
                .getSingleResult();

        assertThat(user.getName(), equalTo(updated.getName()));
        assertThat(user.getEmail(), equalTo(updated.getEmail()));
    }

    @Test
    void findById() {
        UserDto saved = userService.save(userDto);

        UserDto foundById = userService.findById(saved.getId());

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :savedId", User.class);
        User user = query
                .setParameter("savedId", saved.getId())
                .getSingleResult();

        assertThat(user.getName(), equalTo(foundById.getName()));
        assertThat(user.getEmail(), equalTo(foundById.getEmail()));
    }

    @Test
    void findAll() {
        UserDto userDtoSecond = UserDto.builder()
                .name("Second")
                .email("second@ya.ru")
                .build();

        UserDto userDtoThird = UserDto.builder()
                .name("Third")
                .email("third@ya.ru")
                .build();

        userService.save(userDto);
        userService.save(userDtoSecond);
        userService.save(userDtoThird);

        List<UserDto> returned = userService.findAll();

        TypedQuery<User> query = em.createQuery("Select u from User u", User.class);
        List<User> users = query
                .getResultList();

        assertThat(returned.size(), equalTo(3));
        assertThat(returned.get(0).getEmail(), equalTo(users.get(0).getEmail()));
        assertThat(returned.get(0).getName(), equalTo(users.get(0).getName()));
        assertThat(returned.get(2).getEmail(), equalTo(users.get(2).getEmail()));
        assertThat(returned.get(2).getName(), equalTo(users.get(2).getName()));
    }

    @Test
    void delete() {
        UserDto saved = userService.save(userDto);

        assertThat(saved.getId(), notNullValue());

        userService.delete(saved.getId());

        User user = em.find(User.class, saved.getId());

        Assertions.assertNull(user);
    }
}