package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorStorage directorStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("feedDbStorage") FeedStorage feedStorage, MpaStorage mpaStorage,
                       GenreStorage genreStorage, DirectorStorage directorStorage) {
        this.userStorage = userStorage;
        this.feedStorage = feedStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorStorage = directorStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        checkName(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        checkName(user);
        checkUserId(user.getId());
        return userStorage.updateUser(user);
    }

    public void deleteUser(Long userId) {
        checkUserId(userId);
        userStorage.deleteUser(userId);
    }

    public void addFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        userStorage.addFriend(userId, friendId);
        feedStorage.addFeed(friendId, userId, EventType.FRIEND, Operation.ADD);
    }

    public void removeFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователь(id = {}) удалён из друзей.", friendId);
        feedStorage.addFeed(friendId, userId, EventType.FRIEND, Operation.REMOVE);
    }

    public User getUserById(Long id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            throw new UserNotFoundException("Пользователь  с ID = " + id + " не найден.");
        }
        return user;
    }

    public List<User> getUserFriendsById(Long id) {
        checkUserId(id);
        return userStorage.getUserFriendsById(id);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        checkUserId(userId);
        checkUserId(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    public List<Feed> getFeedByUserId(Long id) {
        checkUserId(id);
        return feedStorage.getFeedByUserId(id);
    }

    public List<Film> getRecommendationsByUserId(Long id) {
        checkUserId(id);
        Map<Long, Set<Film>> mapLikeFilms = userStorage.getLikesFilms();
        if (mapLikeFilms.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Set<Film> userLikeFilms = mapLikeFilms.get(id); //достаем понравившиеся фильмы нашего пользователя
        mapLikeFilms.remove(id); // удаляем данные нашего пользователя из мапы
        long maxCommonLikes = 0L;
        long checkIdUser = 0L;

        for (Long userId : mapLikeFilms.keySet()) { // перебираем оставшихся пользователей
            Set<Film> otherLikeFilms = mapLikeFilms.get(userId);

            long count = 0;
            for (Film f : otherLikeFilms) {  // проверяем совпадение фильмов
                if (userLikeFilms.contains(f)) {
                    count++;
                }
            }
            if (count > maxCommonLikes) { // обновляем количество максимальных совпадений лайков ...
                maxCommonLikes = count;
                count = 0;
                checkIdUser = userId;//... и запоминаем айди нужного пользователя
            }
        }
        if (maxCommonLikes == 0) {
            return Collections.EMPTY_LIST;
        }
        // достаем фильмы наиболее подходящего пользователя и удаляем из списка фильмы которые уже лайкнуты
        Set<Film> recomendUserFilms = mapLikeFilms.get(checkIdUser);

        for (Film f : userLikeFilms) {
            recomendUserFilms.remove(f);
        }
        if (recomendUserFilms.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        updateGenreAndMpaAndDirector(recomendUserFilms);
        return recomendUserFilms.stream().collect(Collectors.toUnmodifiableList());
    }

    private void updateGenreAndMpaAndDirector(Collection<Film> films) {
        List<Long> filmIds = new ArrayList<>();
        for (Film film : films) {
            filmIds.add(film.getId());
        }
        Map<Long, Set<Genre>> genres = genreStorage.getGenreMap(filmIds);
        Map<Long, Mpa> mpaMap = mpaStorage.getMpaMap(filmIds);
        Map<Long, Set<Director>> directors = directorStorage.getDirectorMap(filmIds);
        filmIds.clear();
        for (Film film : films) {
            Long filmId = film.getId();
            Set<Genre> genreSet = genres.get(filmId);
            Mpa mpa = mpaMap.get(filmId);
            Set<Director> directorSet = directors.get(filmId);
            if (genreSet != null && genreSet.size() != 0) {
                film.setGenres(genreSet);
            }
            if (mpa != null) {
                film.setMpa(mpa);
            }
            if (directorSet != null && directorSet.size() != 0) {
                film.setDirectors(directorSet);
            }
        }
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void checkUserId(Long id) {
        if (id < 1 || userStorage.getUser(id) == null) {
            throw new UserNotFoundException("Пользователь  с ID = " + id + " не найден.");
        }
    }
}
