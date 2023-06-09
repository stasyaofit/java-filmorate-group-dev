package ru.yandex.practicum.filmorate.storage.friend;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendStorage {
    void addFriend(Long userId, Long friendId);
    void deleteFriend(Long userId, Long friendId);
    List<User> getUserFriendsById(Long userId);
    List<Long> getUserFriendsIds(Long userId);
    List<User> getCommonFriends(Long userId, Long otherId);
}
