package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository("feedDbStorage")
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Feed> getFeedByUserId(Long id) {
        String sql = "SELECT * FROM FEED WHERE USER_ID = ? ORDER BY CREATED_TS";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFeed(rs), id);
    }

    @Override
    public void addFeed(Long entityId, Long userId, EventType eventType, Operation operation) {
        String sql = "INSERT INTO FEED(ENTITY_ID, USER_ID, EVENT_TYPE, OPERATION) VALUES ( ?, ?, ?, ?);";
        jdbcTemplate.update(sql, entityId, userId, eventType.toString(), operation.toString());
    }

    private Feed mapRowToFeed(ResultSet rs) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(rs.getLong("EVENT_ID"));
        feed.setEntityId(rs.getLong("ENTITY_ID"));
        feed.setUserId(rs.getLong("USER_ID"));
        feed.setTimestamp(rs.getTimestamp("CREATED_TS").toInstant().toEpochMilli());
        feed.setEventType(EventType.valueOf(rs.getString("EVENT_TYPE")));
        feed.setOperation(Operation.valueOf(rs.getString("OPERATION")));
        return feed;
    }
}
