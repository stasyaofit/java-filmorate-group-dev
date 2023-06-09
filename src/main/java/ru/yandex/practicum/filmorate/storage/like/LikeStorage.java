package ru.yandex.practicum.filmorate.storage.like;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface LikeStorage {
    void addLike(Long filmId, Long userId);
    void removeLike(Long filmId, Long userId);
    List<Long> getLikes(Long filmId);
    List<Film> getTopNPopularFilms(Long count);
}
