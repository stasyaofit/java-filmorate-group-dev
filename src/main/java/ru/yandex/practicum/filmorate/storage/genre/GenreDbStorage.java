package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM GENRES ORDER BY GENRE_ID";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public Genre getGenre(Integer id) {
        Genre genre = null;
        String sql = "SELECT * FROM GENRES WHERE GENRE_ID = ? ORDER BY GENRE_ID";
        List<Genre> genreList = jdbcTemplate.query(sql, this::mapRowToGenre, id);
        if (genreList.size() != 0) {
            genre = genreList.get(0);
        }
        return genre;
    }

    @Override
    // новый метод, добавляет сразу все жанры
    public void addFilmGenres(Film film) {
        Set<Genre> genres = new LinkedHashSet<>(film.getGenres());
        String sql = "INSERT INTO FILM_GENRES (FILM_ID, GENRE_ID) VALUES (?, ?);";
        jdbcTemplate.batchUpdate(sql, genres, genres.size(),
                (PreparedStatement ps, Genre genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, genre.getId());
                }
        );
    }

    @Override
    public void deleteGenresFromFilm(Long filmId) {
        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE FILM_ID = ?", filmId);
    }

    @Override
    public Map<Long, Set<Genre>> getGenreMap(List<Long> ids) {
        String sql = "SELECT FG.FILM_ID, FG.GENRE_ID, GENRE_NAME " +
                "FROM FILM_GENRES AS FG JOIN GENRES as G on FG.GENRE_ID = G.GENRE_ID " +
                "WHERE FG.FILM_ID IN (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        final Map<Long, Set<Genre>> genreMap = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            long filmId = rs.getLong("film_id");
            Genre genre = mapRowToGenre(rs, 0);
            Set<Genre> genres = genreMap.getOrDefault(filmId, new HashSet<>());
            genres.add(genre);
            genreMap.put(filmId, genres);
        });
        return genreMap;
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("GENRE_ID"), rs.getString("GENRE_NAME"));
    }
}
