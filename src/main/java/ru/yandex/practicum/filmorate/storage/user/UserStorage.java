package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserStorage {

    Collection<User> findAll();

    User getUser(Long id);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long userId);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<User> getUserFriendsById(Long userId);

    List<User> getCommonFriends(Long userId, Long otherId);

    Map<Long, Set<Film>> getLikesFilms();
}
