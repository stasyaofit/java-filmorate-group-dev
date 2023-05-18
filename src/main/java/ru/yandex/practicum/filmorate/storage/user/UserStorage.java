package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {

    Collection<User> findAll();

    User getUser(Long id);

    User createUser(User user);

    User updateUser(User user);

    List<User> getUserFriendsById(Long id);

    List<User> getCommonFriends(Long userId, Long otherId);
}
