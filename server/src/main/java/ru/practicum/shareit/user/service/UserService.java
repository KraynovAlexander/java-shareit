package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto save(UserDto user);

    UserDto update(long id, UserDto user);

    UserDto findById(long id);

    List<UserDto> findAll();

    void delete(long id);
}
