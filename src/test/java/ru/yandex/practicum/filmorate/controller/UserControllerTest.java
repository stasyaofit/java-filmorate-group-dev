package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

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
        controller = new UserController();
        user = getValidUser();
        updateUser = getUpdateValidUser();
    }

    private User getValidUser() {
        return User.builder()
                .id(0)
                .email("example@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.now())
                .build();
    }

    private User getUpdateValidUser() {
        return User.builder()
                .id(1)
                .email("another@mail.ru")
                .login("new_login")
                .name("")
                .birthday(LocalDate.now())
                .build();
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
        final int id = controller.createUser(user).getId();
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
        final ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateUser(user));
        assertEquals(ValidationException.class, ex.getClass());
    }

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
