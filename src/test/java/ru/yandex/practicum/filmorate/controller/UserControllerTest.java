package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
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

class UserControllerTest {
    private UserController controller;
    private User user;
    private User updateUser;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        UserService service = new UserService(userStorage, filmStorage);
        controller = new UserController(service);
        user = getValidUser();
        updateUser = getUpdateValidUser();
    }

    private User getValidUser() {
        User user = new User();
        user.setId(0L);
        user.setEmail("example@mail.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now());
        return user;
    }

    private User getUpdateValidUser() {
        User updateUser = new User();
        updateUser.setId(1L);
        updateUser.setEmail("another@mail.ru");
        updateUser.setLogin("new_login");
        updateUser.setName("");
        updateUser.setBirthday(LocalDate.now());
        return updateUser;
    }

    private void validateInput(User user) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @DisplayName("Создание валидного пользователя и обновление на пользователя без отображаемого имени")
    @Test
    void createAndUpdateValidUser() {
        final Long id = controller.createUser(user).getId();

        user.setId(id);
        assertEquals(user, controller.findAll().get(0));
        System.out.println(controller.findAll().get(0));

        controller.updateUser(updateUser);
        assertEquals(updateUser, controller.findAll().get(0));
        System.out.println(controller.findAll().get(0));
    }

    @DisplayName("Обновление несуществующего пользователя")
    @Test
    void updateNotFoundIdUser() {
        final UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> controller.updateUser(user));
        assertEquals(UserNotFoundException.class, ex.getClass());
    }

    //
    @DisplayName("Валидация пользователя с пустой или некорректной почтой")
    @ParameterizedTest
    @ValueSource(strings = {"", "login mail"})
    void validateEmptyOrNotCorrectEmailUser(String email) {
        user.setEmail(email);
        final ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validateInput(user));
        assertEquals(ConstraintViolationException.class, ex.getClass());
    }

    @DisplayName("Валидация пользователя: с пустым логином/ логин из пробелов/ логин, содержащий пробел")
    @ParameterizedTest
    @ValueSource(strings = {"", "     ", "Nick Name"})
    void validateEmptyOrNotCorrectLoginUser(String login) {
        user.setLogin(login);
        final ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validateInput(user));
        assertEquals(ConstraintViolationException.class, ex.getClass());
    }

    @DisplayName("Валидация пользователя с датой рождения в будущем")
    @Test
    void validateFutureBirthdayUser() {
        user.setBirthday(LocalDate.of(2450, 2, 25));
        final ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validateInput(user));
        assertEquals(ConstraintViolationException.class, ex.getClass());
    }
}
