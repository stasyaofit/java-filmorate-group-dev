package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository("reviewDbStorage")
@Slf4j
public class ReviewDbStorage implements ReviewStorage{

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Review createReview(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("REVIEW_ID");
        Long id = simpleJdbcInsert.executeAndReturnKey(review.toMap()).longValue();
        log.info("Отзыв с ID = {} успешно добавлен.", id);
        return getReview(id);
    }

    @Override
    public List<Review> getTopNReviews(Long count) {
        String getPopularQuery = "R.*, L.RATING as USEFUL "
                + "FROM REVIEWS AS R "
                + "LEFT JOIN (SELECT REVIEW_ID,SUM(LIKE_RATING) as RATING FROM REVIEW_LIKES GROUP BY REVIEW_ID) L "
                + "ORDER BY L.RATING DESC LIMIT ?";
        return jdbcTemplate.query(getPopularQuery, this::mapRowToReview, count);
    }

    @Override
    public List<Review> getTopNReviewsByFilmId(Long filmId, Long count) {
        String getPopularQuery = "R.*, L.RATING as USEFUL "
                + "FROM REVIEWS AS R "
                + "LEFT JOIN (SELECT REVIEW_ID,SUM(LIKE_RATING) as RATING FROM REVIEW_LIKES GROUP BY REVIEW_ID) L "
                + "WHERE FILM_ID = ? "
                + "ORDER BY L.RATING DESC LIMIT ?";
        return jdbcTemplate.query(getPopularQuery, this::mapRowToReview, filmId, count);
    }

    @Override
    public Review getReview(Long reviewId) {
        Review review = null;
        String sql = "SELECT REVIEWS.*, L.RATING as USEFUL "
                + "ROM REVIEWS "
                + "LEFT JOIN (SELECT REVIEW_ID,SUM(LIKE_RATING) as RATING FROM REVIEW_LIKES GROUP BY REVIEW_ID) L "
                + "on L.REVIEW_ID = REVIEWS.REVIEW_ID "
                + "WHERE REVIEW_ID = ?";
        List<Review> reviewList = jdbcTemplate.query(sql, this::mapRowToReview, reviewId);
        if (reviewList.size() != 0) {
            review = reviewList.get(0);
        }
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sqlQuery = "UPDATE REVIEWS "
                + "SET CONTENT = ?, FILM_ID = ?, USER_ID = ? WHERE FILM_ID = ?";
        if (jdbcTemplate.update(sqlQuery, review.getContent(), review.getFilmId(), review.getUserId(),
                review.getReviewId()) != 0) {
            return review;
        } else {
            return null;
        }
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        String sql = "INSERT INTO REVIEW_LIKES (REVIEW_ID, USER_ID, LIKE_RATING) VALUES (?, ?, 1)";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        String sql = "INSERT INTO REVIEW_LIKES (REVIEW_ID, USER_ID, LIKE_RATING) VALUES (?, ?, -1)";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    @Override
    public void deleteReview(Long reviewId) {
        if (jdbcTemplate.update("DELETE FROM REVIEWS WHERE REVIEW_ID = ? ", reviewId) > 0) {
            log.info("Отзыв с ID={} успешно удален", reviewId);
        }
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        if (jdbcTemplate.update("DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ? AND LIKE_RATING > 0",
                reviewId, userId) > 0) {
            log.info("Лайк отзыву с ID={} от пользователя с ID={} успешно удален", reviewId, userId);
        }
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        if (jdbcTemplate.update("DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ? AND LIKE_RATING < 0",
                reviewId, userId) > 0) {
            log.info("Дизлайу отзыву с ID={} от пользователя с ID={} успешно удален", reviewId, userId);
        }
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("REVIEW_ID"));
        review.setContent(rs.getString("CONTENT"));
        review.setFilmId(rs.getLong("FILM_ID"));
        review.setUserId(rs.getLong("USER_ID"));
        review.setUseful(rs.getLong("USEFUL"));
        return review;
    }
}
