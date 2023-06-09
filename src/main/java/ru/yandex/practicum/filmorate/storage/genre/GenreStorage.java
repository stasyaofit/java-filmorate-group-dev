package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;

public interface GenreStorage {
    Collection<Genre> findAll();

    Genre getGenre(Integer id);

    void delete(Film film);

    void add(Film film);

    List<Genre> getFilmGenres(Long filmId);
}
