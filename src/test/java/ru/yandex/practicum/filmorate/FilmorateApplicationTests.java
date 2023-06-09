package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureCache
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)

class FilmorateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final FilmService filmService;
    private final UserService userService;

    private User firstUser;
    private User secondUser;
    private User thirdUser;
    private Film firstFilm;
    private Film secondFilm;
    private Film thirdFilm;

    @BeforeEach
    public void beforeEach() {
        firstUser = new User();
        firstUser.setEmail("1@ya.ru");
        firstUser.setLogin("Mr.First");
        firstUser.setName("First");
        firstUser.setBirthday(LocalDate.of(1980, 12, 23));

        secondUser = new User();
        secondUser.setEmail("2@ya.ru");
        secondUser.setLogin("Mr.Second");
        secondUser.setName("Second");
        secondUser.setBirthday(LocalDate.of(1990, 12, 24));

        thirdUser = new User();
        thirdUser.setEmail("3@ya.ru");
        thirdUser.setLogin("Mr.Third");
        thirdUser.setName("Third");
        thirdUser.setBirthday(LocalDate.of(2000, 12, 25));

        firstFilm = new Film();
        firstFilm.setName("Босиком по мостовой");
        firstFilm.setDescription("Подробное описание");
        firstFilm.setReleaseDate(LocalDate.of(2005, 10, 5));
        firstFilm.setDuration(100);
        firstFilm.setMpa(new Mpa(1, "G"));
        firstFilm.setGenres(new HashSet<>(Arrays.asList(new Genre(1, "Комедия"),
                new Genre(2, "Драма"))));

        secondFilm = new Film();
        secondFilm.setName("Джон Уик");
        secondFilm.setDescription("Подробное описание");
        secondFilm.setReleaseDate(LocalDate.of(2015, 10, 5));
        secondFilm.setDuration(110);
        secondFilm.setMpa(new Mpa(5, "NC-17"));
        secondFilm.setGenres(new HashSet<>(List.of(new Genre(6, "Боевик"))));

        thirdFilm = new Film();
        thirdFilm.setName("Титаник");
        thirdFilm.setDescription("Подробное описание");
        thirdFilm.setReleaseDate(LocalDate.of(1992, 10, 5));
        thirdFilm.setDuration(130);
        thirdFilm.setMpa(new Mpa(4, "R"));
        thirdFilm.setGenres(new HashSet<>(List.of(new Genre(2, "Драма"))));

    }

    @Test
    public void testCreateUserAndGetUserById() {
        firstUser = userStorage.createUser(firstUser);
        Optional<User> userOptional = Optional.ofNullable(userStorage.getUser(firstUser.getId()));
        assertThat(userOptional)
                .hasValueSatisfying(user ->
                        assertThat(user)
                                .hasFieldOrPropertyWithValue("id", firstUser.getId())
                                .hasFieldOrPropertyWithValue("login", "Mr.First"));
    }

    @Test
    public void testGetUsers() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        Collection<User> listUsers = userStorage.findAll();
        assertThat(listUsers).contains(firstUser);
        assertThat(listUsers).contains(secondUser);
    }

    @Test
    public void testUpdateUser() {
        firstUser = userStorage.createUser(firstUser);
        User updateUser = new User();
        updateUser.setId(firstUser.getId());
        updateUser.setEmail("1@ya.ru");
        updateUser.setLogin("Mr.First");
        updateUser.setName("updFirst");
        updateUser.setBirthday(LocalDate.of(1980, 12, 23));

        Optional<User> testUpdateUser = Optional.ofNullable(userStorage.updateUser(updateUser));
        assertThat(testUpdateUser)
                .hasValueSatisfying(user -> assertThat(user)
                        .hasFieldOrPropertyWithValue("name", "updFirst")
                );
    }

    @Test
    public void deleteUser() {
        firstUser = userStorage.createUser(firstUser);
        userStorage.deleteUser(firstUser.getId());
        Collection<User> listUsers = userStorage.findAll();
        assertThat(listUsers).hasSize(0);
    }

    @Test
    public void testCreateFilmAndGetFilmById() {
        firstFilm = filmStorage.createFilm(firstFilm);
        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilm(firstFilm.getId()));
        assertThat(filmOptional)
                .hasValueSatisfying(film -> assertThat(film)
                        .hasFieldOrPropertyWithValue("id", firstFilm.getId())
                        .hasFieldOrPropertyWithValue("name", "Босиком по мостовой")
                );
    }

    @Test
    public void testGetFilms() {
        firstFilm = filmStorage.createFilm(firstFilm);
        secondFilm = filmStorage.createFilm(secondFilm);
        thirdFilm = filmStorage.createFilm(thirdFilm);
        Collection<Film> listFilms = filmStorage.findAll();
        assertThat(listFilms).contains(firstFilm);
        assertThat(listFilms).contains(secondFilm);
        assertThat(listFilms).contains(thirdFilm);
    }

    @Test
    public void testUpdateFilm() {
        firstFilm = filmStorage.createFilm(firstFilm);
        Film updateFilm = new Film();
        updateFilm.setId(firstFilm.getId());
        updateFilm.setName("UpdateName");
        updateFilm.setDescription("UpdateDescription");
        updateFilm.setReleaseDate(LocalDate.of(2015, 10, 5));
        updateFilm.setDuration(110);
        updateFilm.setMpa(new Mpa(1, "G"));
        secondFilm.setGenres(new HashSet<>(List.of(new Genre(6, "Боевик"))));

        Optional<Film> testUpdateFilm = Optional.ofNullable(filmStorage.updateFilm(updateFilm));
        assertThat(testUpdateFilm)
                .hasValueSatisfying(film ->
                        assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "UpdateName")
                                .hasFieldOrPropertyWithValue("description", "UpdateDescription")
                );
    }

    @Test
    public void deleteFilm() {
        firstFilm = filmStorage.createFilm(firstFilm);
        filmStorage.deleteFilm(firstFilm.getId());
        Collection<Film> listFilms = filmStorage.findAll();
        assertThat(listFilms).hasSize(0);
    }

    @Test
    public void testAddLike() {
        firstUser = userStorage.createUser(firstUser);
        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.addLike(firstFilm.getId(), firstUser.getId());
        firstFilm = filmStorage.getFilm(firstFilm.getId());
        assertThat(firstFilm.getLikes()).hasSize(1);
        assertThat(firstFilm.getLikes()).contains(firstUser.getId());
    }

    @Test
    public void testDeleteLike() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.addLike(firstFilm.getId(), firstUser.getId());
        filmService.addLike(firstFilm.getId(), secondUser.getId());
        filmService.removeLike(firstFilm.getId(), firstUser.getId());
        firstFilm = filmStorage.getFilm(firstFilm.getId());
        assertThat(firstFilm.getLikes()).hasSize(1);
        assertThat(firstFilm.getLikes()).contains(secondUser.getId());
    }

    @Test
    public void testGetTopNPopularFilms() {

        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);

        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.addLike(firstFilm.getId(), firstUser.getId());

        secondFilm = filmStorage.createFilm(secondFilm);
        filmService.addLike(secondFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), secondUser.getId());
        filmService.addLike(secondFilm.getId(), thirdUser.getId());

        thirdFilm = filmStorage.createFilm(thirdFilm);
        filmService.addLike(thirdFilm.getId(), firstUser.getId());
        filmService.addLike(thirdFilm.getId(), secondUser.getId());

        List<Film> listFilms = filmService.getTopNPopularFilms(5L);

        assertThat(listFilms).hasSize(3);

        assertThat(Optional.of(listFilms.get(0)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Джон Уик"));

        assertThat(Optional.of(listFilms.get(1)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Титаник"));

        assertThat(Optional.of(listFilms.get(2)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Босиком по мостовой"));
    }

    @Test
    public void testAddFriend() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        assertThat(userService.getUserFriendsById(firstUser.getId())).hasSize(1);
        assertThat(userService.getUserFriendsById(firstUser.getId())).contains(secondUser);
    }
    @Test
    public void testDeleteFriend() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        userService.removeFriend(firstUser.getId(), secondUser.getId());
        assertThat(userService.getUserFriendsById(firstUser.getId())).hasSize(1);
        assertThat(userService.getUserFriendsById(firstUser.getId())).contains(thirdUser);
    }

    @Test
    public void testGetFriends() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        assertThat(userService.getUserFriendsById(firstUser.getId())).hasSize(2);
        assertThat(userService.getUserFriendsById(firstUser.getId())).contains(secondUser, thirdUser);
    }

    @Test
    public void testGetCommonFriends() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        userService.addFriend(secondUser.getId(), firstUser.getId());
        userService.addFriend(secondUser.getId(), thirdUser.getId());
        assertThat(userService.getCommonFriends(firstUser.getId(), secondUser.getId())).hasSize(1);
        assertThat(userService.getCommonFriends(firstUser.getId(), secondUser.getId()))
                .contains(thirdUser);
    }
}

