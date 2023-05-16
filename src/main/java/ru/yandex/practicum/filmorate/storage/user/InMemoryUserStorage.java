package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь с ID = {} успешно добавлен.", user.getId());

        return user;
    }

    @Override
    public User updateUser(User user) {
        log.info("Получен PUT-запрос к эндпоинту '/users' на обновления данных пользователя с ID = {}.", user.getId());
        if (users.containsKey(user.getId())) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            log.info("Пользователь с ID = {} успешно обновлён.", user.getId());
        } else {
            throw new UserNotFoundException(String.format("Пользователя с id: %s не существует", user.getId()));
        }
        return user;
    }

    public Map<Long, User> getUsers() {
        return users;
    }
}
