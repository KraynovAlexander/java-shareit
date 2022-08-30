package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errorHandler.exception.*;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto save(UserDto userDto) {
        User user = userRepository.save(UserMapper.toUser(userDto));
        log.info("Пользователь с id={} добавлено успешно", user.getId());

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        User beingUpdated = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с id=%s не найден", id)));

        if (userDto.getEmail() != null) {
            checkForDuplication(userDto.getEmail());
            beingUpdated.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) beingUpdated.setName(userDto.getName());

        userRepository.save(beingUpdated);
        log.info("Пользователь с id={} успешно обновлено", id);

        return UserMapper.toUserDto(beingUpdated);
    }

    @Override
    public UserDto findById(long id) {
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с  id=%s не найден", id))));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(long id) {
        userRepository.deleteById(id);
        log.info("Пользователь с  id={} успешно удален", id);
    }

    private void checkForDuplication(String email) {
        if (userRepository.existsUserByEmail(email))
            throw new UserException(String.format("Пользователь с электронной почтой=%s уже существует", email));
    }
}