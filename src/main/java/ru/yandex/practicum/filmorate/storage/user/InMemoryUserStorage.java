package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public User getUser(Long id) {
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public User createUser(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь с ID = {} успешно добавлен.", user.getId());

        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        log.info("Пользователь с ID = {} успешно обновлён.", user.getId());

        return user;
    }

    public List<User> getUserFriendsById(Long id) {
        User user = getUser(id);
        return findAll().stream()
                .filter(user1 -> user.getFriends().contains(user1.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
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
