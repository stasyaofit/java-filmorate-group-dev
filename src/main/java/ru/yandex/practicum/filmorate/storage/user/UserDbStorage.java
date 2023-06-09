package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Repository("userDbStorage")
@Slf4j

public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendStorage friendStorage;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate, FriendStorage friendStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.friendStorage = friendStorage;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM USERS";
        return Collections.unmodifiableCollection(jdbcTemplate.query(sql, this::mapRowToUser));
    }

    @Override
    public User getUser(Long id) {
        User user = null;
        List<User> userList = jdbcTemplate.query("SELECT * FROM USERS WHERE USER_ID = ?", this::mapRowToUser, id);
        if (userList.size() != 0) {
            user = userList.get(0);
        }
        return user;
    }

    @Override
    public User createUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");
        user.setId(simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue());
        log.info("Пользователь с ID = {} успешно добавлен.", user.getId());
        return user;

    }

    @Override
    public User updateUser(User user) {
        String sqlQuery = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info("Пользователь с ID={} успешно обновлен", user.getId());
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        if (jdbcTemplate.update("DELETE FROM USERS WHERE USER_ID = ? ", userId) > 0) {
            log.info("Пользователь с ID={} успешно удален", userId);
        }
    }

    public User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("USER_ID"));
        user.setEmail(rs.getString("EMAIL"));
        user.setLogin(rs.getString("LOGIN"));
        user.setName(rs.getString("NAME"));
        user.setBirthday(rs.getDate("BIRTHDAY").toLocalDate());
        user.setFriends(new HashSet<>(friendStorage.getUserFriendsIds(rs.getLong("USER_ID")))); // доделать

        return user;
    }
}
