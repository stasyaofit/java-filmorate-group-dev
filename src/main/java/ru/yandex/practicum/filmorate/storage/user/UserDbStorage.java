package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository("userDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM USERS";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public User getUser(Long id) {
        User user = null;
        String sql = "SELECT * FROM USERS WHERE USER_ID = ?";
        List<User> userList = jdbcTemplate.query(sql, this::mapRowToUser, id);
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
        Long id = simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue();
        log.info("Пользователь с ID = {} успешно добавлен.", id);
        return getUser(id);
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

    @Override
    public void addFriend(Long userId, Long friendId) {
        boolean status = false;
        if (checkUserFriend(friendId, userId)) {
            status = true;  // дружба стала взаимной
            String sql = "UPDATE FRIENDS SET STATUS = ? WHERE USER_ID = ? AND FRIEND_ID = ?";
            jdbcTemplate.update(sql, true, friendId, userId);
        }
        // добавлена заявка в друзья
        String sql = "INSERT INTO FRIENDS (USER_ID, FRIEND_ID, STATUS) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, status);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sql, userId, friendId);
        if (checkUserFriend(friendId, userId)) {
            // дружба стала невзаимной - нужно поменять статус
            sql = "UPDATE FRIENDS SET STATUS = ? " +
                    "WHERE USER_ID = ? AND FRIEND_ID = ?";
            jdbcTemplate.update(sql, false, friendId, userId);
        }
    }

    @Override
    public List<User> getUserFriendsById(Long userId) {
        String sql = "SELECT FRIEND_ID, U.* FROM FRIENDS AS F" +
                " INNER JOIN USERS AS U ON F.FRIEND_ID = U.USER_ID WHERE F.USER_ID = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(Long id1, Long id2) {
        final String q = "SELECT F.FRIEND_ID,U.* FROM (" +
                "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ? INTERSECT " +
                "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ?" +
                ") F INNER JOIN USERS U ON F.FRIEND_ID = U.USER_ID";
        return jdbcTemplate.query(q, this::mapRowToUser, id1, id2);
    }

    private boolean checkUserFriend(Long userId, Long friendId) {
        String sql = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("FRIEND_ID"), userId, friendId).size() > 0;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("USER_ID"));
        user.setEmail(rs.getString("EMAIL"));
        user.setLogin(rs.getString("LOGIN"));
        user.setName(rs.getString("NAME"));
        user.setBirthday(rs.getDate("BIRTHDAY").toLocalDate());
        return user;
    }
}
