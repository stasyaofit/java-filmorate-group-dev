package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        checkFilmReleaseDate(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        checkFilmReleaseDate(film);
        checkFilmId(film.getId());
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(Long id) {
        checkFilmId(id);
        return filmStorage.getFilm(id);
    }

    public void addLike(Long filmId, Long userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        log.info("Пользователь(id = {}) хочет поставить лайк фильму c id: {} .", userId, filmId);
        Film film = filmStorage.getFilm(filmId);
        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
        log.info("Лайк фильму {} успешно добавлен.", film.getName());

    }

    public void removeLike(Long filmId, Long userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        log.info("Пользователь(id = {}) хочет отменить лайк фильму c id: {} .", userId, filmId);
        Film film = filmStorage.getFilm(filmId);
        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            filmStorage.updateFilm(film);
            log.info("Лайк фильму {} успешно удалён.", film.getName());

        }
    }

    public List<Film> getTopNPopularFilms(Long count) {
        if (count < 0) {
            log.error("Количество фильмов не может быть отрицательным.");
            throw new IncorrectParameterException("count");
        }
        return filmStorage.getTopNPopularFilms(count);
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
}
