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
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaService mpaService, GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        updateGenreAndMpa((List<Film>) films);
        updateLikes((List<Film>) films);
        return films;
    }

    public Film createFilm(Film film) {
        checkFilmReleaseDate(film);
        Film createdFilm = filmStorage.createFilm(film);
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreService.addGenreToFilm(createdFilm.getId(), genre.getId()));
        }
        updateGenresByFilm(createdFilm);
        log.info("Добавили фильм: {}", createdFilm.getName());
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        checkFilmReleaseDate(film);
        checkFilmId(film.getId());
        filmStorage.updateFilm(film);
        if (film.getGenres() != null) {
            genreService.deleteGenresFromFilm(film.getId());
            Set<Genre> genres = film.getGenres();
            genres.forEach(genre -> genreService.addGenreToFilm(film.getId(), genre.getId()));
            genres.clear();
            updateGenresByFilm(film);
        }
        if (film.getMpa() != null) {
            film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
        }
        updateFilmLikes(film);
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
        updateGenresByFilm(film);
        updateFilmLikes(film);
        if (film.getMpa() != null) {
            film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
        }
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
        if (film.getLikes().contains(userId)) {
            filmStorage.removeLike(filmId, userId);
            log.info("Лайк фильму {} успешно удалён.", film.getName());
        }
    }

    public List<Film> getTopNPopularFilms(Long count) {
        if (count < 0) {
            log.error("Количество фильмов не может быть отрицательным.");
            throw new IncorrectParameterException("count");
        }
        List<Film> films = filmStorage.getTopNPopularFilms(count);
        updateGenreAndMpa(films);
        updateLikes(films);
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

    private void updateGenresByFilm(Film film) {
        List<Long> filmIds = new ArrayList<>();
        filmIds.add(film.getId());
        Map<Long, Set<Genre>> genreMap = genreService.getGenreMap(filmIds);
        if (genreMap.get(film.getId()) != null) {
            film.setGenres(genreMap.get(film.getId()));
        }
    }

    private void updateFilmLikes(Film film) {
        List<Long> likes = filmStorage.getLikes(film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    private void updateLikes(List<Film> films) {
        for (Film film : films) {
            updateFilmLikes(film);
        }
    }

    private void updateGenreAndMpa(List<Film> films) {
        List<Long> filmIds = new ArrayList<>();
        for (Film film : films) {
            filmIds.add(film.getId());
        }
        Map<Long, Set<Genre>> genres = genreService.getGenreMap(filmIds);
        Map<Long, Mpa> mpa = mpaService.getMpaMap(filmIds);
        filmIds.clear();
        if (films.size() != 0) {
            for (Film film : films) {
                if (genres.get(film.getId()) != null) {
                    film.setGenres(genres.get(film.getId()));
                }
                if (mpa.get(film.getId()) != null) {
                    film.setMpa(mpa.get(film.getId()));
                }
            }
        }
    }

}
