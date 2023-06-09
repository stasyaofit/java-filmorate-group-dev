package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private FriendStorage friendStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage,
                       FriendStorage friendStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.friendStorage = friendStorage;
    }

    public UserService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        checkName(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        checkName(user);
        checkUserId(user.getId());
        return userStorage.updateUser(user);
    }

    public void deleteUser(Long userId) {
        checkUserId(userId);
        userStorage.deleteUser(userId);
    }

    public void addFriend(Long id, Long friendId) {
        checkUserId(id);
        checkUserId(friendId);
        friendStorage.addFriend(id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        checkUserId(id);
        checkUserId(friendId);
        friendStorage.deleteFriend(id, friendId);
        log.info("Пользователь(id = {}) удалён из друзей.", friendId);

    }

    public User getUserById(Long id) {
        checkUserId(id);
        return userStorage.getUser(id);
    }

    public List<User> getUserFriendsById(Long id) {
        checkUserId(id);
        return friendStorage.getUserFriendsById(id);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        checkUserId(userId);
        checkUserId(otherId);
        return friendStorage.getCommonFriends(userId, otherId);
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void checkUserId(Long id) {
        if (id < 1 || userStorage.getUser(id) == null) {
            throw new UserNotFoundException("Пользователь  с ID = " + id + " не найден.");
        }
    }
}
