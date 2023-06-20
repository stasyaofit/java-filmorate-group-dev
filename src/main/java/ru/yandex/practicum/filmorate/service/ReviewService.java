package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;

    private final UserStorage userStorage;

    private final FilmStorage filmStorage;

    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.reviewStorage = reviewStorage;
    }

    public Collection<Review> findTopNReviews(Long count) {
        if (count < 0) {
            log.error("Количество отзвывов не может быть отрицательным.");
            throw new IncorrectParameterException("count");
        }
        return reviewStorage.getTopNReviews(count);
    }

    public Collection<Review> findTopNReviewsByFilmId(Long filmId, Long count) {
        if (count < 0) {
            log.error("Количество отзвывов не может быть отрицательным.");
            throw new IncorrectParameterException("count");
        }
        checkFilmId(filmId);
        return reviewStorage.getTopNReviewsByFilmId(filmId,count);
    }

    public Review createReview(Review review) {
        Long reviewId = reviewStorage.createReview(review).getReviewId();
        review.setReviewId(reviewId);
        log.info("Добавили отзыв: {}", review);
        return review;
    }

    public Review updateReview(Review review) {
        checkReviewId(review.getReviewId());
        checkFilmId(review.getFilmId());
        checkUserId(review.getReviewId());
        reviewStorage.updateReview(review);
        log.info("Обновлен отзыв c id = {}", review.getReviewId());
        return review;
    }

    public void deleteReview(Long reviewId) {
        log.info("Удален отзыв c id = {}",reviewId);
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReviewById(Long reviewId) {
        Review review = reviewStorage.getReview(reviewId);
        if (review == null) {
            throw new ReviewNotFoundException("Отзыв с ID = " + reviewId + " не найден.");
        }
        return review;
    }

    public void addLike(Long reviewId, Long userId) {
        checkReviewId(userId);
        checkUserId(userId);
        reviewStorage.addLike(reviewId,userId);
        log.info("Пользователь(id = {}) поставил лайк отзыву c id: {} .", userId, reviewId);
    }

    public void addDislike(Long reviewId, Long userId) {
        checkReviewId(userId);
        checkUserId(userId);
        reviewStorage.addDislike(reviewId,userId);
        log.info("Пользователь(id = {}) поставил дизлайк отзыву c id: {} .", userId, reviewId);
    }

    public void removeLike(Long reviewId, Long userId) {
        checkReviewId(userId);
        checkUserId(userId);
        reviewStorage.removeLike(reviewId,userId);
        log.info("Пользователь(id = {}) удалил лайк отзыву c id: {} .", userId, reviewId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        checkReviewId(userId);
        checkUserId(userId);
        reviewStorage.removeDislike(reviewId,userId);
        log.info("Пользователь(id = {}) удалил дизлайк отзыву c id: {} .", userId, reviewId);
    }

    private void checkReviewId(Long id) {
        if (id < 1 || reviewStorage.getReview(id) == null) {
            throw new UserNotFoundException("Отзыв  с ID = " + id + " не найден.");
        }
    }

    private void checkFilmId(Long id) {
        if (id < 1 || filmStorage.getFilm(id) == null) {
            throw new FilmNotFoundException("Фильм с ID = " + id + " не найден.");
        }
    }

    private void checkUserId(Long id) {
        if (id < 1 || userStorage.getUser(id) == null) {
            throw new UserNotFoundException("Пользователь  с ID = " + id + " не найден.");
        }
    }
}
