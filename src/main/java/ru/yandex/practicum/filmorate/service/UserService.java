package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Data
@Slf4j
public class UserService {
    private UserStorage userStorage;
    private FilmStorage filmStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage, InMemoryFilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void addFriend(Long id, Long friendId) {
        if (id < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь с ID = " + id + " не найден.");
        }
        if (friendId < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь с ID = " + friendId + " не найден.");
        }
        Map<Long, User> users = userStorage.getUsers();
        if (users.containsKey(id) && users.containsKey(friendId)) {
            User user = users.get(id);
            User friend = users.get(friendId);
            user.getFriends().add(friendId);
            userStorage.updateUser(user);
            friend.getFriends().add(id);
            userStorage.updateUser(friend);
            log.info("Пользователь(id = {}) добавлен в друзья.", friendId);
        } else {
            throw new UserNotFoundException("Пользователь не найден.");
        }
    }

    public void removeFriend(Long id, Long friendId) {
        if (id < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь с ID = " + id + " не найден.");
        }
        if (friendId < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь с ID = " + friendId + " не найден.");
        }
        Map<Long, User> users = userStorage.getUsers();
        if (users.containsKey(id) && users.containsKey(friendId)) {
            User user = users.get(id);
            User friend = users.get(friendId);
            user.getFriends().remove(friendId);
            userStorage.updateUser(user);
            friend.getFriends().remove(id);
            userStorage.updateUser(friend);
            log.info("Пользователь(id = {}) удалён из друзей.", friendId);
        } else {
            throw new UserNotFoundException("Пользователь не найден.");
        }
    }

    public User getUserById(Long id) {
        if (id < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь с ID = " + id + " не найден.");
        }
        if (!userStorage.getUsers().containsKey(id)) {
            throw new UserNotFoundException("Пользователь не найден.");
        }
        return userStorage.getUsers().get(id);
    }

    public List<User> getUserFriendsById(Long id) {
        if (id < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь с ID = " + id + " не найден.");
        }
        if (!userStorage.getUsers().containsKey(id)) {
            throw new UserNotFoundException("Пользователь c id: %s не найден.");
        }
        User user = userStorage.getUsers().get(id);
        return userStorage.getUsers().values().stream()
                .filter(user1 -> user.getFriends().contains(user1.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (userId < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь с ID = " + userId + " не найден.");
        }
        if (otherId < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь с ID = " + otherId + " не найден.");
        }
        List<User> commonFriends = new ArrayList<>();
        List<User> userFriends = getUserFriendsById(userId);
        List<User> otherUserFriends = getUserFriendsById(otherId);
        if (userFriends.size() == 0 || otherUserFriends.size() == 0) {
            log.info("У пользователя(id = {}) нет общих с друзей с пользователем(id = {}).", userId, otherId);
        } else {
            commonFriends = userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toList());
            if (commonFriends.size() == 0) {
                log.info("У пользователя(id = {}) нет общих с друзей с пользователем(id = {}).", userId, otherId);
            } else {
                log.info("У пользователя(id = {}) кол-во общих с друзей с пользователем(id = {}) - {}.",
                        userId, otherId, commonFriends.size());
            }
        }
        return commonFriends;
    }
}
