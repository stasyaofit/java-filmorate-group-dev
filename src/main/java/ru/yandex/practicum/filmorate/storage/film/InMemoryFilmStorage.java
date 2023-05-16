package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film createFilm(Film film) {
        if (isValidFilm(film)) {
            film.setId(nextId++);
            films.put(film.getId(), film);
            log.info("Фильм с ID= {} успешно добавлен.", film.getId());
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (isValidFilm(film)) {
            if (films.containsKey(film.getId())) {
                films.put(film.getId(), film);
                log.info("Фильм с ID = {} успешно обновлён.", film.getId());
            } else {
                throw new FilmNotFoundException(String.format("Фильма с id: %s не найден.", film.getId()));
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


    public Map<Long, Film> getFilms() {
        return films;
    }
}
