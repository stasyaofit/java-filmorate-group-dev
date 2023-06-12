package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM FILMS";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film getFilm(Long id) {
        Film film = null;
        String sql = "SELECT * FROM FILMS WHERE FILM_ID = ?";
        List<Film> filmList = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        if (filmList.size() != 0) {
            film = filmList.get(0);
            if (film.getGenres() == null) {
                film.setGenres(Collections.emptySet());
            }
        }
        return film;
    }

    @Override
    public Film createFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");
        Long id = simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue();
        log.info("Фильм с ID = {} успешно добавлен.", id);
        return getFilm(id);
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlQuery = "UPDATE films SET " +
                "FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, " +
                "RATING_ID = ? WHERE FILM_ID = ?";
        if (jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId()) != 0) {
            return film;
        } else {
            return null;
        }
    }

    @Override
    public void deleteFilm(Long id) {
        if (jdbcTemplate.update("DELETE FROM FILMS WHERE FILM_ID = ? ", id) > 0) {
            log.info("Фильм с ID={} успешно удален", id);
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM FILM_LIKES WHERE FILM_ID = ? AND USER_ID = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Long> getLikes(Long filmId) {
        String sql = "SELECT USER_ID FROM FILM_LIKES WHERE FILM_ID = ? ORDER BY USER_ID";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("USER_ID"), filmId);
    }

    @Override
    public List<Film> getTopNPopularFilms(Long count) {
        String getPopularQuery = "SELECT F.FILM_ID, F.FILM_NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID " +
                "FROM FILMS AS F LEFT JOIN FILM_LIKES FL ON F.FILM_ID = FL.FILM_ID " +
                "GROUP BY F.FILM_ID ORDER BY COUNT(FL.USER_ID) DESC LIMIT ?";
        return jdbcTemplate.query(getPopularQuery, this::mapRowToFilm, count);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("FILM_ID"));
        film.setName(rs.getString("FILM_NAME"));
        film.setDescription(rs.getString("DESCRIPTION"));
        film.setReleaseDate(rs.getDate("RELEASE_DATE").toLocalDate());
        film.setDuration(rs.getInt("DURATION"));
        film.setMpa(new Mpa(rs.getInt("RATING_ID"), null));
        return film;
    }
}
