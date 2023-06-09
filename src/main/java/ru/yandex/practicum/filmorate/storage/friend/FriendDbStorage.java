package ru.yandex.practicum.filmorate.storage.friend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashSet;
import java.util.List;

@Repository
public class FriendDbStorage implements FriendStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FriendDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    public void addFriend(Long userId, Long friendId) {
        boolean status = false;
        if (getUserFriendsIds(friendId).contains(userId)) {
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
        if (getUserFriendsIds(friendId).contains(userId)) {
            // дружба стала невзаимной - нужно поменять статус
            sql = "UPDATE FRIENDS SET STATUS = ? " +
                    "WHERE USER_ID = ? AND FRIEND_ID = ?";
            jdbcTemplate.update(sql, false, friendId, userId);
        }
    }

    @Override
    public List<Long> getUserFriendsIds(Long userId) {
        String sql = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("FRIEND_ID"), userId);
    }

    @Override
    public List<User> getUserFriendsById(Long userId) {
        String sql = "SELECT FRIEND_ID, U.* FROM FRIENDS AS F" +
                " INNER JOIN USERS AS U ON F.FRIEND_ID = U.USER_ID WHERE F.USER_ID = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                        rs.getLong("FRIEND_ID"),
                        rs.getString("EMAIL"),
                        rs.getString("LOGIN"),
                        rs.getString("NAME"),
                        rs.getDate("BIRTHDAY").toLocalDate(),
                        new HashSet<>(getUserFriendsIds(rs.getLong("FRIEND_ID")))),
                userId
        );
    }

    public List<User> getCommonFriends(Long id1, Long id2) {
        final String q = "SELECT F.FRIEND_ID,U.* FROM (" +
                "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ? INTERSECT " +
                "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ?" +
                ") F INNER JOIN USERS U ON F.FRIEND_ID = U.USER_ID";
        return jdbcTemplate.query(q, (rs, rowNum) -> new User(
                rs.getLong("FRIEND_ID"),
                rs.getString("EMAIL"),
                rs.getString("LOGIN"),
                rs.getString("NAME"),
                rs.getDate("BIRTHDAY").toLocalDate(),
                new HashSet<>(getUserFriendsIds(rs.getLong("FRIEND_ID")))), id1, id2);
    }
}
