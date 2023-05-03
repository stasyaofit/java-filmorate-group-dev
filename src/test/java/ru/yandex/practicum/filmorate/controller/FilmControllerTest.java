package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.*;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    private FilmController controller;
    private Film film;
    private Film updateFilm;
    private final LocalDate failDateRelease = LocalDate.of(1895, 12, 27);
    private final LocalDate minDateRelease = LocalDate.of(1895, 12, 28);

    @BeforeEach
    void setUp() {
        controller = new FilmController();
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
        return Film.builder()
                .id(0)
                .name("Гиперболоид инженера Гарина")
                .description("Фильм по одноименной книге Алексея Толстого")
                .releaseDate(LocalDate.of(1965, 8, 12))
                .duration(96)
                .build();
    }

    private Film getValidUpdateFilm() {
        return Film.builder()
                .id(1)
                .name("Другое название")
                .description("Другое описание")
                .releaseDate(minDateRelease)
                .duration(20)
                .build();
    }

    @DisplayName("Создание и обновление валидного фильма")
    @Test
    void createAndUpdateValidFilm() {
        controller.createFilm(film);
        film.setId(1);
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
        controller.createFilm(film);
        film.setId(1);
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
        final ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateFilm(film));
        assertEquals(ValidationException.class, ex.getClass());
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
        controller.createFilm(film);
        film.setId(1);
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