package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {

    Collection<Film> findAll();

    Film getFilm(Long id);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    boolean deleteFilm(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getCommonFilms(Long userId, Long friendId);

    Map<Long, Set<Long>> getLikeMap(List<Long> ids);

    List<Film> getFilmsByDirector(Integer directorId);

    List<Film> getFilmsDirectorSortByYear(Integer directorId);

    List<Film> getFilmsDirectorSortByLikes(Integer directorId);

    List<Film> searchFilmsByNameOrDirector(String textQuery, List<String> searchParams);

    List<Film> getTopNPopularFilms(Integer count, Integer genreId, Integer year);

    Map<Long, Set<Film>> getLikesFilms();

    // набросал функции чисто так, что текло из головы, а в крови другое, пришёл с др =)))
    boolean addMark(Long filmId, Long userId, Integer mark);

    boolean updateMark(Long filmId, Long userId, Integer mark);

    boolean deleteMark(Long filmId, Long userId, Integer mark);

    // методы ниже отчасти дублируют  друг друга
    // мапа: фильм и список его оценок
    Map<Long, List<Integer>> getFilmsMarks(Long filmId);

    /* мапа: фильм и его средняя оценка ( если по старой схеме то,
    понадобиться для обновления оценок у фильма, вкупе с жанрами и режиссёрами)
     */
    Map<Long, Double> getFilmsAvgMarks(Long filmId);

        /* Список фильмов пользователя с положительными оценками, может понадобится для рекомендаций
    (думаю можно вынести отдельный метод, либо из getFilmsMarks(Long filmId) по условию отобрать,
    ещё как вариант добавить доп.поле(isPositive) в таблицу film_marks, которое характеризует оценку)
     */
    List<Film> getFilmsByUserWhereMarkIsPositive(Long userId);

}
