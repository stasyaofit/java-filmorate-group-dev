package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private int nextId = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен POST-запрос к эндпоинту '/users' на добавление пользователя: {}.", user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь с ID = {} успешно добавлен.", user.getId());

        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен PUT-запрос к эндпоинту '/users' на обновления данных пользователя с ID = {}.", user.getId());
        if (users.containsKey(user.getId())) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            log.info("Пользователь с ID = {} успешно обновлён.", user.getId());

        } else {
            throw new ValidationException(String.format("Пользователя с id: %s не существует", user.getId()));
        }
        return user;
    }
}
