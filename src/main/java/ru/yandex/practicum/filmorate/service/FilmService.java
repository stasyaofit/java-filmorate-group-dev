package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        updateGenreAndMpaAndLike(films);
        return films;
    }

    public Film createFilm(Film film) {
        checkFilmReleaseDate(film);
        Long filmId = filmStorage.createFilm(film).getId();
        film.getGenres().forEach(genre -> genreStorage.addGenreToFilm(filmId, genre.getId()));
        film.setId(filmId);
        log.info("Добавили фильм: {}", film.getName());
        return film;
    }

    public Film updateFilm(Film film) {
        checkFilmReleaseDate(film);
        checkFilmId(film.getId());
        filmStorage.updateFilm(film);
        genreStorage.deleteGenresFromFilm(film.getId());
        film.getGenres().forEach(genre -> genreStorage.addGenreToFilm(film.getId(), genre.getId()));
        updateGenreAndMpaAndLike(List.of(film));

        log.info("Обновлен фильм c id = {}", film.getId());
        return film;
    }

    public void deleteFilm(Long id) {
        checkFilmId(id);
        filmStorage.deleteFilm(id);
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.getFilm(id);
        if (film == null) {
            throw new FilmNotFoundException("Фильм с ID = " + id + " не найден.");
        }
        updateGenreAndMpaAndLike(List.of(film));
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        log.info("Пользователь(id = {}) хочет поставить лайк фильму c id: {} .", userId, filmId);
        Film film = getFilmById(filmId);
        filmStorage.addLike(filmId, userId);

        log.info("Лайк фильму {} успешно добавлен.", film.getName());
    }

    public void removeLike(Long filmId, Long userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        log.info("Пользователь(id = {}) хочет отменить лайк фильму c id: {} .", userId, filmId);
        Film film = getFilmById(filmId);
        filmStorage.removeLike(filmId, userId);
        log.info("Лайк фильму {} успешно удалён.", film.getName());
    }

    public List<Film> getTopNPopularFilms(Long count) {
        if (count < 0) {
            log.error("Количество фильмов не может быть отрицательным.");
            throw new IncorrectParameterException("count");
        }
        List<Film> films = filmStorage.getTopNPopularFilms(count);
        updateGenreAndMpaAndLike(films);
        return films;
    }

    private void checkFilmReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }

    private void checkFilmId(Long id) {
        if (id < 1 || filmStorage.getFilm(id) == null) {
            throw new FilmNotFoundException("Фильм с ID = " + id + " не найден.");
        }
    }

    private void checkUserId(Long id) {
        if (id < 1 || userStorage.getUser(id) == null) {
            throw new UserNotFoundException("Пользователь  с ID = " + id + " не найден.");
        }
    }

    private void updateGenreAndMpaAndLike(Collection<Film> films) {
        List<Long> filmIds = new ArrayList<>();
        for (Film film : films) {
            filmIds.add(film.getId());
        }
        Map<Long, Set<Genre>> genres = genreStorage.getGenreMap(filmIds);
        Map<Long, Mpa> mpaMap = mpaStorage.getMpaMap(filmIds);
        Map<Long, Set<Long>> likes = filmStorage.getLikeMap(filmIds);
        filmIds.clear();
        for (Film film : films) {
            Set<Genre> genreSet = genres.get(film.getId());
            Mpa mpa = mpaMap.get(film.getId());
            Set<Long> likeSet = likes.get(film.getId());
            if (genreSet != null) {
                film.setGenres(genreSet);
            }
            if (mpa != null) {
                film.setMpa(mpa);
            }
            if (likeSet != null) {
                film.setLikes(likeSet);
            }
        }
    }
}
