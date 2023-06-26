package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String getCommonQuery = "SELECT * FROM films f WHERE film_id IN " +
                "(SELECT l1.film_id FROM film_likes l1, film_likes l2 " +
                "WHERE l1.film_id = l2.film_id AND l1.user_id = ? AND l2.user_id = ? " +
                "GROUP BY l1.film_id ORDER BY COUNT(l1.user_id) DESC)";
        return jdbcTemplate.query(getCommonQuery, this::mapRowToFilm, userId, friendId);
    }

    @Override
    public Map<Long, Set<Long>> getLikeMap(List<Long> ids) {
        String sql = "SELECT FILM_ID, USER_ID FROM FILM_LIKES WHERE FILM_ID IN (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        final Map<Long, Set<Long>> likeMap = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            Long filmId = rs.getLong("FILM_ID");
            Long likeId = rs.getLong("USER_ID");
            Set<Long> likes = likeMap.getOrDefault(filmId, new HashSet<>());
            likes.add(likeId);
            likeMap.put(filmId, likes);
        });
        return likeMap;
    }

    @Override
    public List<Film> getTopNPopularFilms(Integer count, Integer genreId, Integer year) {
        List<Film> filmsPopular;
        if (genreId != null && year != null) {
            String sql = "SELECT F.* FROM films AS F " +
                    "LEFT JOIN film_genres AS FG ON FG.film_id = F.film_id " +
                    "LEFT JOIN genres AS G ON G.genre_id = FG.genre_id " +
                    "LEFT JOIN film_likes AS FL ON FL.film_id = F.film_id " +
                    "WHERE G.genre_id = ? AND YEAR(release_date) = ? " +
                    "GROUP BY F.film_id " +
                    "ORDER BY COUNT(FL.user_id) DESC " +
                    "LIMIT ?";
            filmsPopular = jdbcTemplate.query(sql, this::mapRowToFilm, genreId, year, count);
            log.info("Популярные фильмы по жанру {}, году {} успешно получены", genreId, year);
        } else if (year != null) {
            String sql = "SELECT F.* FROM films AS F " +
                    "LEFT JOIN film_genres AS FG ON FG.film_id = F.film_id " +
                    "LEFT JOIN genres AS G ON G.genre_id = FG.genre_id " +
                    "LEFT JOIN film_likes AS FL ON FL.film_id = F.film_id " +
                    "WHERE YEAR(release_date) = ? " +
                    "GROUP BY F.film_id " +
                    "ORDER BY COUNT(FL.user_id) DESC " +
                    "LIMIT ?";
            filmsPopular = jdbcTemplate.query(sql, this::mapRowToFilm, year, count);
            log.info("Популярные фильмы по году {} успешно получены", year);
        } else if (genreId != null) {
            String sql = "SELECT F.* FROM films AS F " +
                    "LEFT JOIN film_genres AS FG ON FG.film_id = F.film_id " +
                    "LEFT JOIN genres AS G ON G.genre_id = FG.genre_id " +
                    "LEFT JOIN film_likes AS FL ON FL.film_id = F.film_id " +
                    "WHERE G.genre_id = ? " +
                    "GROUP BY F.film_id " +
                    "ORDER BY COUNT(FL.user_id) DESC " +
                    "LIMIT ?";
            filmsPopular = jdbcTemplate.query(sql, this::mapRowToFilm, genreId, count);
            log.info("Популярные фильмы по жанру {} успешно получены", genreId);
        } else {
            String sql = "SELECT F.* FROM films AS F " +
                    "LEFT JOIN film_genres AS FG ON FG.film_id = F.film_id " +
                    "LEFT JOIN genres AS G ON G.genre_id = FG.genre_id " +
                    "LEFT JOIN film_likes AS FL ON FL.film_id = F.film_id " +
                    "GROUP BY F.film_id " +
                    "ORDER BY COUNT(FL.user_id) DESC " +
                    "LIMIT ?";
            filmsPopular = jdbcTemplate.query(sql, this::mapRowToFilm, count);
            log.info("Популярные фильмы успешно получены");
        }
        return filmsPopular;
    }

    @Override
    public List<Film> getFilmsByDirector(Integer directorId) {
        String sql = "SELECT F.*, MR.RATING_NAME, FD.DIRECTOR_ID, D.DIRECTOR_NAME \n" +
                "FROM FILMS AS F \n" +
                "LEFT JOIN FILM_DIRECTOR FD ON F.FILM_ID = FD.FILM_ID \n" +
                "LEFT JOIN DIRECTOR D ON D.DIRECTOR_ID = FD.DIRECTOR_ID \n" +
                "LEFT JOIN MPA_RATINGS MR ON MR.RATING_ID = F.RATING_ID \n" +
                "WHERE D.DIRECTOR_ID = ?;";

        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

    @Override
    public List<Film> getFilmsDirectorSortByYear(Integer directorId) {
        String sql = "SELECT F.*, MR.RATING_NAME, FD.DIRECTOR_ID, D.DIRECTOR_NAME \n" +
                "FROM FILMS AS F \n" +
                "LEFT JOIN FILM_DIRECTOR FD ON F.FILM_ID = FD.FILM_ID \n" +
                "LEFT JOIN DIRECTOR D ON D.DIRECTOR_ID = FD.DIRECTOR_ID \n" +
                "LEFT JOIN MPA_RATINGS MR ON MR.RATING_ID = F.RATING_ID \n" +
                "WHERE D.DIRECTOR_ID = ?\n" +
                "ORDER BY YEAR(F.RELEASE_DATE);";

        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

    @Override
    public List<Film> getFilmsDirectorSortByLikes(Integer directorId) {
        String sql = "SELECT F.*, MR.RATING_NAME, FD.DIRECTOR_ID, D.DIRECTOR_NAME, COUNT(FL.USER_ID) " +
                "FROM FILMS AS F " +
                "LEFT JOIN FILM_LIKES FL ON F.FILM_ID = FL.FILM_ID " +
                "LEFT JOIN FILM_DIRECTOR FD ON F.FILM_ID = FD.FILM_ID " +
                "LEFT JOIN DIRECTOR D ON D.DIRECTOR_ID = FD.DIRECTOR_ID " +
                "LEFT JOIN MPA_RATINGS MR ON MR.RATING_ID = F.RATING_ID " +
                "WHERE D.DIRECTOR_ID = ? " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(FL.USER_ID) DESC;";

        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

    @Override
    public List<Film> searchFilmsByNameOrDirector(String textQuery, List<String> searchParams) {
        String textQuerySQL = "%" + textQuery + "%";
        if (searchParams.contains("title") && searchParams.contains("director")) {
            String sql = "SELECT F.*, MR.RATING_NAME, FD.DIRECTOR_ID,\n" +
                    "D.DIRECTOR_NAME, COUNT(FL.USER_ID)\n" +
                    "FROM FILMS AS F\n" +
                    "LEFT JOIN FILM_LIKES FL ON F.FILM_ID = FL.FILM_ID\n" +
                    "LEFT JOIN FILM_DIRECTOR FD ON F.FILM_ID = FD.FILM_ID\n" +
                    "LEFT JOIN DIRECTOR D ON D.DIRECTOR_ID = FD.DIRECTOR_ID\n" +
                    "LEFT JOIN MPA_RATINGS MR ON MR.RATING_ID = F.RATING_ID\n" +
                    "WHERE LOWER(F.FILM_NAME) LIKE LOWER(?) OR LOWER(D.DIRECTOR_NAME) LIKE LOWER(?)\n" +
                    "GROUP BY F.FILM_ID\n" +
                    "ORDER BY COUNT(FL.USER_ID) DESC;";
            return jdbcTemplate.query(sql, this::mapRowToFilm,
                    textQuerySQL, textQuerySQL);
        } else if (searchParams.contains("director")) {
            String sql = "SELECT * FROM FILMS f " +
                    "JOIN MPA_RATINGS M ON M.RATING_ID = F.RATING_ID " +
                    "JOIN FILM_DIRECTOR FD ON FD.FILM_ID = F.FILM_ID " +
                    "JOIN DIRECTOR D ON D.DIRECTOR_ID = FD.DIRECTOR_ID " +
                    "JOIN FILM_LIKES AS FL ON FL.FILM_ID = F.FILM_ID " +
                    "WHERE LOWER(D.DIRECTOR_NAME) LIKE LOWER(?) " +
                    "GROUP BY F.FILM_ID " +
                    "ORDER BY COUNT(FL.USER_ID) DESC ";
            return jdbcTemplate.query(sql, this::mapRowToFilm, textQuerySQL);
        } else if (searchParams.contains("title")) {
            String sql = "SELECT F.*, MR.RATING_NAME, COUNT(FL.USER_ID)\n" +
                    "FROM FILMS AS F\n" +
                    "LEFT JOIN FILM_LIKES FL ON F.FILM_ID = FL.FILM_ID\n" +
                    "LEFT JOIN MPA_RATINGS MR ON MR.RATING_ID = F.RATING_ID\n" +
                    "WHERE LOWER(F.FILM_NAME) LIKE LOWER(?)\n" +
                    "GROUP BY F.FILM_ID\n" +
                    "ORDER BY COUNT(FL.USER_ID);";
            return jdbcTemplate.query(sql, this::mapRowToFilm, textQuerySQL);
        } else {
            return null;
        }
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
