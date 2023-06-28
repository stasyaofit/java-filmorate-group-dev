package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

public interface FeedStorage {
    List<Feed> getFeedByUserId(Long id);

    void addFeed(Long entityId, Long userId, EventType eventType, Operation operation);
}

