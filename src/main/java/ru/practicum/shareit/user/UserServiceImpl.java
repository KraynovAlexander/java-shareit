package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserException;
import ru.practicum.shareit.exception.UserException;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private long id;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto save(UserDto user) {
        checkForDuplication(user.getEmail());

        user.setId(++id);
        userRepository.save(user);
        log.info("Пользователь с id={} добавлен успешно", user.getId());

        return user;
    }

    @Override
    public UserDto update(long id, UserDto user) {
        UserDto beingUpdated = userRepository.findById(id);

        if (user.getEmail() != null) {
            checkForDuplication(user.getEmail());
            beingUpdated.setEmail(user.getEmail());
        }
        if (user.getName() != null) beingUpdated.setName(user.getName());

        userRepository.update(beingUpdated);
        log.info("Пользователь с id={} успешн обновлено", user.getId());

        return beingUpdated;
    }

    @Override
    public UserDto findById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void delete(long id) {
        userRepository.delete(id);
        log.info("Пользователь с id={} удален успешно", id);
    }

    private void checkForDuplication(String email) {
        if (userRepository.doesEmailExist(email))
            throw new UserException(String.format("Пользователь с электронной почтой=%s уже существует", email));
    }
}