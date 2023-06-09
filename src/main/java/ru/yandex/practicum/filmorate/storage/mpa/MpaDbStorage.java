package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Collections;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> findAll() {
        String sql = "SELECT * FROM MPA_RATINGS";
        return Collections.unmodifiableCollection(jdbcTemplate.query(sql, (rs, rowNum) -> new Mpa(
                rs.getInt("RATING_ID"),
                rs.getString("RATING_NAME"))
        ));
    }

    @Override
    public Mpa getMpa(Integer id) {
        Mpa mpa = null;
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM MPA_RATINGS WHERE RATING_ID = ?", id);
        if (mpaRows.first()) {
            mpa = new Mpa(
                    mpaRows.getInt("RATING_ID"),
                    mpaRows.getString("RATING_NAME")
            );
        }
        return mpa;
    }
}

