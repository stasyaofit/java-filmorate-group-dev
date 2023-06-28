package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;


    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("feedDbStorage") FeedStorage feedStorage) {
        this.userStorage = userStorage;
        this.feedStorage = feedStorage;
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

    public void addFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        userStorage.addFriend(userId, friendId);
        feedStorage.addFeed(friendId, userId, EventType.FRIEND, Operation.ADD);
    }

    public void removeFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователь(id = {}) удалён из друзей.", friendId);
        feedStorage.addFeed(friendId, userId, EventType.FRIEND, Operation.REMOVE);
    }

    public User getUserById(Long id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            throw new UserNotFoundException("Пользователь  с ID = " + id + " не найден.");
        }
        return user;
    }

    public List<User> getUserFriendsById(Long id) {
        checkUserId(id);
        return userStorage.getUserFriendsById(id);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        checkUserId(userId);
        checkUserId(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    public List<Feed> getFeedByUserId(Long id) {
        checkUserId(id);
        return feedStorage.getFeedByUserId(id);
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkUserId(Long id) {
        if (id < 1 || userStorage.getUser(id) == null) {
            throw new UserNotFoundException("Пользователь  с ID = " + id + " не найден.");
        }
    }
}
