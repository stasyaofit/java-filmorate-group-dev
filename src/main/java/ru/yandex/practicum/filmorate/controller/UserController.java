package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен GET-запрос к эндпоинту '/users' на получение списка всех пользователей.");
        return userService.findAll();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен POST-запрос к эндпоинту '/users' на добавление пользователя: {}.", user);
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен PUT-запрос к эндпоинту '/users' на обновления данных пользователя с ID = {}.", user.getId());
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Получен GET-запрос к эндпоинту '/users/{id}' на получение пользователя с ID = {}.", id);
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен PUT-запрос к эндпоинту '/users/{id}/friends/{friendId}' на добавление в друзья." +
                " пользователя с ID = {} пользователем с ID = {}.", friendId, id);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен DELETE-запрос к эндпоинту '/users/{id}/friends/{friendId}' на удаление из друзей." +
                " пользователя с ID = {} пользователем с ID = {}.", friendId, id);
        userService.removeFriend(id, friendId);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("Получен DELETE-запрос к эндпоинту: '/users' на удаление пользователя с ID={}", id);
        userService.deleteUser(id);
    }


    @GetMapping("/{id}/friends")
    public List<User> getUserFriendsById(@PathVariable Long id) {
        log.info("Получен GET-запрос к эндпоинту '/users/{id}/friends' на получение " +
                "списка друзей пользователя с ID = {}.", id);
        return userService.getUserFriendsById(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получен GET-запрос к эндпоинту '/users/{id}/friends/common/{otherId}' на получение " +
                "списка общих друзей у пользователя с ID = {} с пользователем с ID = {}.", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
