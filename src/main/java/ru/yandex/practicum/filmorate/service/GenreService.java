package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.*;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    @Autowired
    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre getGenreById(Integer id) {
        Genre genre = genreStorage.getGenre(id);
        if (genre == null) {
            throw new GenreNotFoundException("Жанр с ID = " + id + " не найден.");
        }
        return genre;
    }

    public void addGenreToFilm(Long filmId, Integer genreId) {
        genreStorage.addGenreToFilm(filmId, genreId);
    }

    public void deleteGenresFromFilm(Long filmId) {
        genreStorage.deleteGenresFromFilm(filmId);
    }

    public Map<Long, Set<Genre>> getGenreMap(List<Long> ids) {
        return genreStorage.getGenreMap(ids);
    }
}
