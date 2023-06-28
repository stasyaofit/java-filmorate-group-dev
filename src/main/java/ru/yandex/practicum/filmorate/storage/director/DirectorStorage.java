package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DirectorStorage {
    Director createDirector(Director director);

    Director updateDirector(Director director);

    boolean deleteDirector(Integer id);

    Collection<Director> findAll();

    Director getDirector(Integer id);

    void addFilmDirectors(Film film);

    void deleteDirectorsFromFilm(Long filmId);

    Map<Long, Set<Director>> getDirectorMap(List<Long> ids);
}
