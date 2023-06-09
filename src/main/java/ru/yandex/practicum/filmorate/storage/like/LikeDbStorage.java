package ru.yandex.practicum.filmorate.storage.like;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.HashSet;
import java.util.List;

@Repository
public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Autowired
    public LikeDbStorage(JdbcTemplate jdbcTemplate, MpaService mpaService, GenreService genreService) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.genreService = genreService;
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
        String sql = "SELECT USER_ID FROM FILM_LIKES WHERE FILM_ID = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("USER_ID"), filmId);
    }

    @Override
    public List<Film> getTopNPopularFilms(Long count) {
        String getPopularQuery = "SELECT F.FILM_ID, F.FILM_NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.RATING_ID " +
                "FROM FILMS AS F LEFT JOIN FILM_LIKES FL ON F.FILM_ID = FL.FILM_ID " +
                "GROUP BY F.FILM_ID ORDER BY COUNT(FL.USER_ID) DESC LIMIT ?";

        return jdbcTemplate.query(getPopularQuery, (rs, rowNum) -> new Film(
                        rs.getLong("FILM_ID"),
                        rs.getString("FILM_NAME"),
                        rs.getString("DESCRIPTION"),
                        rs.getDate("RELEASE_DATE").toLocalDate(),
                        rs.getInt("DURATION"),
                        new HashSet<>(getLikes(rs.getLong("FILM_ID"))),
                        mpaService.getMpaById(rs.getInt("RATING_ID")),
                        new HashSet<>(genreService.getFilmGenres(rs.getLong("FILM_ID")))),
                count);
    }


}
