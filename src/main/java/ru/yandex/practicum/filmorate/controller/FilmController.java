package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private int nextId = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен POST-запрос к эндпоинту '/films' на добавление фильма: {}.", film);
        if (isValidFilm(film)) {
            film.setId(nextId++);
            films.put(film.getId(), film);
            log.info("Фильм с ID= {} успешно добавлен.", film.getId());
        }
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен PUT-запрос к эндпоинту '/films' на обновления фильма с ID = {}", film.getId());
        if (isValidFilm(film)) {
            if (films.containsKey(film.getId())) {
                films.put(film.getId(), film);
                log.info("Фильм с ID = {} успешно обновлён.", film.getId());
            } else {
                throw new ValidationException(String.format("Фильма с id: %s не существует", film.getId()));
            }
        }
        return film;
    }

    private boolean isValidFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        return true;
    }
}
