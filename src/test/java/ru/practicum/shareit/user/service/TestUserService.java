package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import ru.practicum.shareit.errorHandler.exception.UserException;
import ru.practicum.shareit.errorHandler.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.repo.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TestUserService {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    private static UserDto userDto;
    private static User user;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@ya.ru")
                .build();

        user = UserMapper.toUser(userDto);
    }

    @Test
    void whenSaveThenCallSaveRepository() {
        Mockito
                .when(userRepository.save(Mockito.any()))
                .thenReturn(user);

        UserDto returned = userService.save(userDto);

        assertThat(returned, equalTo(UserMapper.toUserDto(user)));

        Mockito.verify(userRepository, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    void whenUpdateUserWithoutEmailThenNotCallExistsByEmailRepository() {
        UserDto userDtoWithoutEmail = UserDto.builder().id(1L).name("New").build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(user);

        UserDto updated = userService.update(1L, userDtoWithoutEmail);

        assertThat(updated, equalTo(UserMapper.toUserDto(user)));
        assertThat(updated.getId(), equalTo(user.getId()));
        assertThat(updated.getEmail(), equalTo(user.getEmail()));
        assertThat(updated.getName(), equalTo("New"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .save(user);
        Mockito.verify(userRepository, Mockito.never())
                .existsUserByEmail(Mockito.anyString());
    }

    @Test
    void whenUpdateUserWithEmailThenCallThreeMethodsRepository() {
        userDto.setEmail("ya@yandex.ru");

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsUserByEmail(Mockito.anyString()))
                .thenReturn(false);
        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(user);

        UserDto updated = userService.update(1L, userDto);

        assertThat(updated, equalTo(UserMapper.toUserDto(user)));
        assertThat(updated.getId(), equalTo(user.getId()));
        assertThat(updated.getEmail(), equalTo("ya@yandex.ru"));
        assertThat(updated.getName(), equalTo(user.getName()));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsUserByEmail(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1))
                .save(user);
    }

    @Test
    void whenUpdateNotExistsUserThenThrowUserNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenThrow(new UserNotFoundException("Пользователь с id=1 не найден"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.update(1L, userDto));

        Assertions.assertEquals("Пользователь с id=1 не найден", exception.getMessage());
    }

    @Test
    void whenUpdateUserWithExistsEmailThenThrowUserException() {
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsUserByEmail("user@ya.ru"))
                .thenThrow(new UserException("Пользователь с электронной почтой=user@ya.ru already exists"));

        final UserException exception = Assertions.assertThrows(
                UserException.class,
                () -> userService.update(1L, userDto));

        Assertions.assertEquals("Пользователь с электронной почтой=user@ya.ru already exists", exception.getMessage());
    }

    @Test
    void whenFindByIdExistsUserThenReturnUser() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDto returned = userService.findById(1L);

        assertThat(returned, equalTo(UserMapper.toUserDto(user)));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
    }

    @Test
    void whenFindByIdNotExistsUserThenThrowUserNotFoundException() {
        Mockito.when(userRepository.findById(5L))
                .thenThrow(new UserNotFoundException("Пользователь с id=5 не найден"));

        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(5L));

        Assertions.assertEquals("Пользователь с id=5 не найден", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(5L);
    }

    @Test
    void whenFindAllThenCallFindAllRepository() {
        Mockito.when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<UserDto> returnedUsers = userService.findAll();

        assertThat(returnedUsers.size(), equalTo(1));
        assertThat(returnedUsers.get(0), equalTo(UserMapper.toUserDto(user)));

        Mockito.verify(userRepository, Mockito.times(1))
                .findAll();
    }

    @Test
    void whenDeleteThenCallDeleteRepository() {
        userService.delete(1L);

        Mockito.verify(userRepository, Mockito.times(1))
                .deleteById(1L);
    }
}