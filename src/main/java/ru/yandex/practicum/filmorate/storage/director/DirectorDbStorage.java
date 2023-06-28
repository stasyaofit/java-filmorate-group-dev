package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Director createDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("DIRECTOR")
                .usingGeneratedKeyColumns("DIRECTOR_ID");
        Integer id = simpleJdbcInsert.executeAndReturnKey(director.toMap()).intValue();
        director.setId(id);
        log.info("Режиссёр с ID = {} успешно добавлен.", id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "UPDATE director SET DIRECTOR_NAME = ? WHERE DIRECTOR_ID = ?";
        if (jdbcTemplate.update(sqlQuery, director.getName(), director.getId()) != 0) {
            return director;
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteDirector(Integer id) {
        return jdbcTemplate.update("DELETE FROM director WHERE DIRECTOR_ID = ? ", id) > 0;
    }

    @Override
    public Collection<Director> findAll() {
        String sql = "SELECT * FROM director";
        return jdbcTemplate.query(sql, this::mapRowToDirector);
    }

    @Override
    public Director getDirector(Integer id) {
        Director director = null;
        String sql = "SELECT * FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        List<Director> directorList = jdbcTemplate.query(sql, this::mapRowToDirector, id);
        if (directorList.size() != 0) {
            director = directorList.get(0);
        }
        return director;
    }

    @Override
    public void addFilmDirectors(Film film) {
        Set<Director> directors = new LinkedHashSet<>(film.getDirectors());
        String sql = "INSERT INTO FILM_DIRECTOR (FILM_ID, DIRECTOR_ID) VALUES (?, ?);";
        jdbcTemplate.batchUpdate(sql, directors, directors.size(),
                (PreparedStatement ps, Director director) -> {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, director.getId());
                }
        );
    }

    @Override
    public void deleteDirectorsFromFilm(Long filmId) {
        jdbcTemplate.update("DELETE FROM FILM_DIRECTOR WHERE FILM_ID = ?", filmId);
    }

    @Override
    // поменял твой load, там было прикольно со стримами, только ты его нигде и не применил)
    public Map<Long, Set<Director>> getDirectorMap(List<Long> ids) {
        String sql = "SELECT FD.FILM_ID, FD.DIRECTOR_ID, DIRECTOR_NAME " +
                "FROM FILM_DIRECTOR AS FD JOIN DIRECTOR as D on FD.DIRECTOR_ID = D.DIRECTOR_ID " +
                "WHERE FD.FILM_ID IN (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        final Map<Long, Set<Director>> directorMap = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            Long filmId = rs.getLong("FILM_ID");
            Director director = mapRowToDirector(rs, 0);
            Set<Director> directors = directorMap.getOrDefault(filmId, new HashSet<>());
            directors.add(director);
            directorMap.put(filmId, directors);
        });
        return directorMap;
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        Integer id = rs.getInt("DIRECTOR_ID");
        String name = rs.getString("DIRECTOR_NAME");
        return new Director(id, name);
    }
}
