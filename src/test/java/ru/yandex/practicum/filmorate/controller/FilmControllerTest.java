package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    private final LocalDate failDateRelease = LocalDate.of(1895, 12, 27);
    private final LocalDate minDateRelease = LocalDate.of(1895, 12, 28);
    private FilmController controller;
    private Film film;
    private Film updateFilm;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        FilmService service = new FilmService(filmStorage, userStorage);
        controller = new FilmController(service);
        film = getValidFilm();
        updateFilm = getValidUpdateFilm();
    }

    private void validateInput(Film film) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private Film getValidFilm() {
        Film film = new Film();
        film.setId(0L);
        film.setName("Гиперболоид инженера Гарина");
        film.setDescription("Фильм по одноименной книге Алексея Толстого");
        film.setReleaseDate(LocalDate.of(1965, 8, 12));
        film.setDuration(96);
        return film;
    }

    private Film getValidUpdateFilm() {
        Film updateFilm = new Film();
        updateFilm.setId(1L);
        updateFilm.setName("Другое название");
        updateFilm.setDescription("Другое описание");
        updateFilm.setReleaseDate(minDateRelease);
        updateFilm.setDuration(20);
        return updateFilm;
    }

    @DisplayName("Создание и обновление валидного фильма")
    @Test
    void createAndUpdateValidFilm() {
        final Long id = controller.createFilm(film).getId();
        film.setId(id);
        assertEquals(film, controller.findAll().get(0));
        System.out.println(controller.findAll());

        controller.updateFilm(updateFilm);
        assertEquals(updateFilm, controller.findAll().get(0));
        System.out.println(controller.findAll());
    }

    @DisplayName("Создание и обновление фильма c min датой релиза")
    @Test
    void createAndUpdateMinReleaseDateFilm() {
        film.setReleaseDate(minDateRelease);
        final Long id = controller.createFilm(film).getId();
        film.setId(id);
        assertEquals(film, controller.findAll().get(0));

        controller.updateFilm(updateFilm);
        assertEquals(updateFilm, controller.findAll().get(0));
    }

    @DisplayName("Создание фильма c невалидной датой релиза")
    @Test
    void createFailReleaseDateFilm() {
        film.setReleaseDate(failDateRelease);
        final ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals(ValidationException.class, ex.getClass());
    }

    @DisplayName("Обновление фильма c невалидной датой релиза")
    @Test
    void updateFailReleaseDateFilm() {
        controller.createFilm(film);
        updateFilm.setReleaseDate(failDateRelease);
        final ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateFilm(updateFilm));
        assertEquals(ValidationException.class, ex.getClass());
    }

    @DisplayName("Обновление фильма c несуществующим id")
    @Test
    void updateNotFoundIdFilm() {
        final FilmNotFoundException ex = assertThrows(FilmNotFoundException.class, () -> controller.updateFilm(film));
        assertEquals(FilmNotFoundException.class, ex.getClass());
    }

    @DisplayName("Валидация фильма с пустым названием")
    @Test
    void validateFailNameFilm() {
        film.setName("");
        final ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validateInput(film));
        assertEquals(ConstraintViolationException.class, ex.getClass());
    }

    @DisplayName("Валидация фильма с пустым описанием")
    @Test
    void validateEmptyDescriptionFilm() {
        film.setDescription("");
        final ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validateInput(film));
        assertEquals(ConstraintViolationException.class, ex.getClass());
    }

    @DisplayName("Создание и обновление фильма с описанием 200 символов")
    @Test
    void createAndUpdateMaxLongDescriptionFilm() {
        film.setDescription(("a").repeat(200));
        final Long id = controller.createFilm(film).getId();
        film.setId(id);
        assertEquals(film, controller.findAll().get(0));
        assertEquals(200, controller.findAll().get(0).getDescription().length());

        updateFilm.setDescription(("a").repeat(200));
        controller.updateFilm(updateFilm);
        assertEquals(updateFilm, controller.findAll().get(0));
        assertEquals(200, controller.findAll().get(0).getDescription().length());
    }

    @DisplayName("Валидация фильма с описанием более 200 символов")
    @Test
    void validateVeryLongDescriptionFilm() {
        film.setDescription(("a").repeat(201));
        final ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validateInput(film));
        assertEquals(ConstraintViolationException.class, ex.getClass());
    }

    @DisplayName("Валидация фильма с отрицательной продолжительность")
    @Test
    void validateNegativeDurationFilm() {
        film.setDuration(-10);
        final ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validateInput(film));
        assertEquals(ConstraintViolationException.class, ex.getClass());
    }

    @DisplayName("Валидация фильма с нулевой продолжительностью")
    @Test
    void validateZeroDurationFilm() {
        film.setDuration(0);
        final ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validateInput(film));
        assertEquals(ConstraintViolationException.class, ex.getClass());
    }
}