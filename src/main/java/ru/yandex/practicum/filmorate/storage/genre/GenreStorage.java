package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GenreStorage {
    Collection<Genre> findAll();

    Genre getGenre(Integer id);

    void deleteGenresFromFilm(Long filmId);

    void addFilmGenres(Film film);

    Map<Long, Set<Genre>> getGenreMap(List<Long> ids);

    List<Genre> findAllById(Long genreId);
}
