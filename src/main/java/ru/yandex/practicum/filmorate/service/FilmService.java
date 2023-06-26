package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final FeedStorage feedStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaStorage mpaStorage, GenreStorage genreStorage, DirectorStorage directorStorage,
                       @Qualifier("feedDbStorage") FeedStorage feedStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.feedStorage = feedStorage;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        updateGenreAndMpaAndLikeAndDirector(films);
        return films;
    }


    public Film createFilm(Film film) {
        checkFilmReleaseDate(film);
        Long filmId = filmStorage.createFilm(film).getId();
        film.setId(filmId);
        putGenreAndDirector(film);
        log.info("Добавили фильм: {}", film.getName());
        return getFilmById(filmId);
    }

    public Film updateFilm(Film film) {
        checkFilmReleaseDate(film);
        checkFilmId(film.getId());
        filmStorage.updateFilm(film);
        deleteGenreAndDirector(film.getId());
        putGenreAndDirector(film);
        log.info("Обновлен фильм c id = {}", film.getId());
        return getFilmById(film.getId());
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
        updateGenreAndMpaAndLikeAndDirector(List.of(film));
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        log.info("Пользователь(id = {}) хочет поставить лайк фильму c id: {} .", userId, filmId);
        Film film = getFilmById(filmId);
        filmStorage.addLike(filmId, userId);
        log.info("Лайк фильму {} успешно добавлен.", film.getName());
        feedStorage.addFeed(filmId, userId, EventType.LIKE, Operation.ADD);
    }

    public void removeLike(Long filmId, Long userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        log.info("Пользователь(id = {}) хочет отменить лайк фильму c id: {} .", userId, filmId);
        Film film = getFilmById(filmId);
        filmStorage.removeLike(filmId, userId);
        log.info("Лайк фильму {} успешно удалён.", film.getName());
        feedStorage.addFeed(filmId, userId, EventType.LIKE, Operation.REMOVE);
    }

    public List<Film> getTopNPopularFilms(Integer count, Integer genreId, Integer year) {
        List<Film> films = new ArrayList<>(filmStorage.getTopNPopularFilms(count, genreId, year));
        updateGenreAndMpaAndLikeAndDirector(films);
        return films;
    }

    public List<Film> getFilmsByDirector(Integer directorId, Optional<String> sortParam) {
        Director director = directorStorage.getDirector(directorId);
        if (director == null) {
            throw new DirectorNotFoundException("Режиссёр с ID = " + directorId + " не найден.");
        }
        List<Film> films;
        if (sortParam.isPresent()) {
            if (sortParam.get().equals("year")) {
                films = new ArrayList<>(filmStorage.getFilmsDirectorSortByYear(directorId));
                updateGenreAndMpaAndLikeAndDirector(films);
                return films;
            } else if (sortParam.get().equals("likes")) {
                films = new ArrayList<>(filmStorage.getFilmsDirectorSortByLikes(directorId));
                updateGenreAndMpaAndLikeAndDirector(films);
                return films;
            } else {
                log.error("Неверный параметр сортировки.");
                throw new IncorrectParameterException("sortParam");
            }
        }
        films = new ArrayList<>(filmStorage.getFilmsByDirector(directorId));
        updateGenreAndMpaAndLikeAndDirector(films);
        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        List<Film> films = new ArrayList<>(filmStorage.getCommonFilms(userId, friendId));
        updateGenreAndMpaAndLikeAndDirector(films);
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

    private void putGenreAndDirector(Film film) {
        genreStorage.addFilmGenres(film);
        directorStorage.addFilmDirectors(film);
    }

    private void deleteGenreAndDirector(Long filmId) {
        genreStorage.deleteGenresFromFilm(filmId);
        directorStorage.deleteDirectorsFromFilm(filmId);
    }

    private void updateGenreAndMpaAndLikeAndDirector(Collection<Film> films) {
        List<Long> filmIds = new ArrayList<>();
        for (Film film : films) {
            filmIds.add(film.getId());
        }
        Map<Long, Set<Genre>> genres = genreStorage.getGenreMap(filmIds);
        Map<Long, Mpa> mpaMap = mpaStorage.getMpaMap(filmIds);
        Map<Long, Set<Long>> likes = filmStorage.getLikeMap(filmIds);
        Map<Long, Set<Director>> directors = directorStorage.getDirectorMap(filmIds);
        filmIds.clear();
        for (Film film : films) {
            Long filmId = film.getId();
            Set<Genre> genreSet = genres.get(filmId);
            Mpa mpa = mpaMap.get(filmId);
            Set<Long> likeSet = likes.get(filmId);
            Set<Director> directorSet = directors.get(filmId);
            if (genreSet != null && genreSet.size() != 0) {
                film.setGenres(genreSet);
            }
            if (mpa != null) {
                film.setMpa(mpa);
            }
            if (likeSet != null && likeSet.size() != 0) {
                film.setLikes(likeSet);
            }
            if (directorSet != null && directorSet.size() != 0) {
                film.setDirectors(directorSet);
            }
        }
    }

    public List<Film> searchFilms(String textQuery, List<String> searchParams) {
        List<Film> searchResult = filmStorage.searchFilmsByNameOrDirector(textQuery, searchParams);
        log.info("Поиск фильма по запросу {} ", textQuery);
        updateGenreAndMpaAndLikeAndDirector(searchResult);
        return searchResult;
    }
}