package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Film getFilm(Long id) {
        return films.get(id);
    }

    @Override
    public Collection<Film> findAll() {
        return Collections.unmodifiableCollection(films.values());
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм с ID= {} успешно добавлен.", film.getId());

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        log.info("Фильм с ID = {} успешно обновлён.", film.getId());

        return film;
    }

    @Override
    public void deleteFilm(Long id) {
        films.remove(id);
        log.info("Фильм с ID={} успешно удален", id);
    }

    public List<Film> getTopNPopularFilms(Long count) {

        return findAll().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
