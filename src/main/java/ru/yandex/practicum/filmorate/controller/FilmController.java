package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен GET-запрос к эндпоинту '/films' на получение списка всех фильмов.");
        return filmService.findAll();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен POST-запрос к эндпоинту '/films' на добавление фильма: {}.", film);
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен PUT-запрос к эндпоинту '/films' на обновления фильма с ID = {}", film.getId());
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Получен GET-запрос к эндпоинту '/films/{id}' на получение фильма с ID = {}.", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен PUT-запрос к эндпоинту '/films/{id}/like/{userId}'." +
                " Пользователь с ID {} ставит лайк фильму с ID {}.", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту '/films/{id}/like/{userId}'." +
                " Пользователь с ID {} удаляет лайк фильму с ID {}.", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getTopNPopularFilms(@RequestParam(required = false) Long count) {
        if (count == null) {
            log.info("Получен GET-запрос к эндпоинту '/films/popular'");
            return filmService.getTopNPopularFilms(10L);
        }
        log.info("Получен GET-запрос к эндпоинту '/films/popular?count={count}' " +
                "на получение топ-{} фильмов.", count);
        return filmService.getTopNPopularFilms(count);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        log.info("Получен DELETE-запрос к эндпоинту: '/films' на удаление фильма с ID={}", id);
        filmService.deleteFilm(id);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Integer directorId, @RequestParam Optional<String> sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}


