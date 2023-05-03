package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
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
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

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

    @DisplayName("Создание валидного пользователя и обновление на пользователя без отображаемого имени")
    @Test
    void createAndUpdateValidUser() {
        controller.createUser(user);
        user.setId(1);
        assertEquals(user, controller.findAll().get(0));
        System.out.println(controller.findAll().get(0));

        controller.updateUser(updateUser);
        updateUser.setName(updateUser.getLogin());
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
    @Test
    void validateEmptyOrNotCorrectEmailUser() {
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "Валидация некорректна");
        System.out.println(violations);

        user.setEmail("login mail");
        violations = validator.validate(user);
        assertEquals(1, violations.size(), "Валидация некорректна");
        System.out.println(violations);
    }

    @DisplayName("Валидация пользователя с пустым логином или состоящим из пробелов")
    @Test
    void validateEmptyOrNotCorrectLoginUser() {
        user.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "Валидация некорректна");

        user.setLogin("     ");
        violations = validator.validate(user);
        assertEquals(1, violations.size(), "Валидация некорректна");
    }

    @DisplayName("Валидация пользователя с датой рождения в будущем")
    @Test
    void validateFutureBirthdayUser() {
        user.setBirthday(LocalDate.of(2450, 2, 25));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "Валидация некорректна");
    }
}
