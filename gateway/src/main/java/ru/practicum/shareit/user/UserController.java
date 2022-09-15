package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("Создание пользователя={}", userDto);
        return userClient.create(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable @Positive long userId,
                                         @RequestBody UserDto userDto) {
        log.info("Обновление пользователя {}, userDto={}", userId, userDto);
        return userClient.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> findById(@PathVariable @Positive long userId) {
        log.info("Получение пользователя {}", userId);
        return userClient.findById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Getting all users");
        return userClient.findAll();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteById(@PathVariable @Positive long userId) {
        log.info("Удаление пользователя {}", userId);
        return userClient.deleteById(userId);
    }
}
