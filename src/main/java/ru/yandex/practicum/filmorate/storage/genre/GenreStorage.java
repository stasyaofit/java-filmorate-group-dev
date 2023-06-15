package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GenreStorage {
    Collection<Genre> findAll();

    Genre getGenre(Integer id);

    void deleteGenresFromFilm(Long filmId);

    void addGenreToFilm(Long filmId, Integer genreId);

    Map<Long, Set<Genre>> getGenreMap(List<Long> ids);
}
