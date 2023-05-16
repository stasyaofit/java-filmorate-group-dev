package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@Slf4j
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(Long id) {
        if (id < 0) {
            log.error("ID не может быть отрицательным.");
            throw new FilmNotFoundException("Фильм с ID " + id + " не найден.");
        }
        if (!filmStorage.getFilms().containsKey(id)) {
            throw new UserNotFoundException("Фильм не найден.");
        }
        return filmStorage.getFilms().get(id);
    }

    public void addLike(Long filmId, Long userId) {
        if (filmId < 0) {
            log.error("ID не может быть отрицательным.");
            throw new FilmNotFoundException("Фильм с ID = " + filmId + " не найден.");
        }
        if (userId < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь c ID = " + userId + " не найден.");
        }
        log.info("Пользователь(id = {}) хочет поставить лайк фильму c id: {} .", userId, filmId);
        if (filmStorage.getFilms().containsKey(filmId)) {
            Film film = filmStorage.getFilms().get(filmId);
            if (userStorage.getUsers().containsKey(userId)) {
                film.getLikes().add(userId);
                filmStorage.updateFilm(film);
                log.info("Лайк фильму {} успешно добавлен.", film.getName());
            } else {
                throw new UserNotFoundException(String.format("Пользователь с id: %s не найден.", userId));
            }
        } else {
            throw new FilmNotFoundException(String.format("Фильма с id: %s не найден.", filmId));
        }
    }

    public void removeLike(Long filmId, Long userId) {
        if (filmId < 0) {
            log.error("ID не может быть отрицательным.");
            throw new FilmNotFoundException("Фильм с ID = " + filmId + " не найден.");
        }
        if (userId < 0) {
            log.error("ID не может быть отрицательным.");
            throw new UserNotFoundException("Пользователь c ID = " + userId + " не найден.");
        }
        log.info("Пользователь(id = {}) хочет отменить лайк фильму c id: {} .", userId, filmId);
        if (filmStorage.getFilms().containsKey(filmId)) {
            Film film = filmStorage.getFilms().get(filmId);
            if (userStorage.getUsers().containsKey(userId) && film.getLikes().contains(userId)) {
                film.getLikes().remove(userId);
                filmStorage.updateFilm(film);
                log.info("Лайк фильму {} успешно удалён.", film.getName());
            } else {
                throw new UserNotFoundException(String.format("Пользователь с id: %s не найден.", userId));
            }
        } else {
            throw new FilmNotFoundException(String.format("Фильма с id: %s не найден.", filmId));
        }
    }

    public List<Film> getTopNPopularFilms(Long count) {
        if (count < 0) {
            log.error("Количество фильмов не может быть отрицательным.");
            throw new IncorrectParameterException("count");
        }
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

}
