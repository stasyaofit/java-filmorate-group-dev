package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Collection<Mpa> findAll() {
        String sql = "SELECT * FROM MPA_RATINGS";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    @Override
    public Mpa getMpa(Integer id) {
        Mpa mpa = null;
        List<Mpa> mpaList = jdbcTemplate.query("SELECT * FROM MPA_RATINGS WHERE RATING_ID = ?",
                this::mapRowToMpa, id);
        if (mpaList.size() != 0) {
            mpa = mpaList.get(0);
        }
        return mpa;
    }

    @Override
    public Map<Long, Mpa> getMpaMap(List<Long> ids) {
        String sql = "SELECT F.* , MR.RATING_NAME FROM FILMS AS F " +
                "LEFT JOIN MPA_RATINGS AS MR ON F.RATING_ID = MR.RATING_ID " +
                "WHERE FILM_ID IN (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        final Map<Long, Mpa> mpaMap = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            Long filmId = rs.getLong("FILM_ID");
            Mpa mpa = mapRowToMpa(rs, 0);
            mpaMap.put(filmId, mpa);
        });
        return mpaMap;
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("RATING_ID"), rs.getString("RATING_NAME"));
    }
}

