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
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
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

    private final FeedStorage feedStorage;

    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("feedDbStorage") FeedStorage feedStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.reviewStorage = reviewStorage;
        this.feedStorage = feedStorage;
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
        return reviewStorage.getTopNReviewsByFilmId(filmId, count);
    }

    public Review createReview(Review review) {
        checkFilmId(review.getFilmId());
        checkUserId(review.getUserId());
        if (review.getIsPositive() == null) {
            throw new IncorrectParameterException("Тип отзыва не может быть пустым");
        }
        Long reviewId = reviewStorage.createReview(review).getReviewId();
        review.setReviewId(reviewId);
        log.info("Добавили отзыв: {}", review);
        feedStorage.addFeed(reviewId, review.getUserId(), EventType.REVIEW, Operation.ADD);
        return getReviewById(reviewId);
    }

    public Review updateReview(Review review) {
        Long reviewId = review.getReviewId();
        Long userId = review.getUserId();
        checkReviewId(reviewId);
        checkFilmId(review.getFilmId());
        checkUserId(userId);
        reviewStorage.updateReview(review);
        log.info("Обновлен отзыв c id = {}", reviewId);
        feedStorage.addFeed(reviewId, userId, EventType.REVIEW, Operation.UPDATE);
        return getReviewById(reviewId);
    }

    public void deleteReview(Long reviewId) {
        checkReviewId(reviewId);
        Long userId = getReviewById(reviewId).getUserId();
        reviewStorage.deleteReview(reviewId);
        log.info("Удален отзыв c id = {}", reviewId);
        checkUserId(userId);
        feedStorage.addFeed(reviewId, userId, EventType.REVIEW, Operation.REMOVE);
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
        reviewStorage.addLike(reviewId, userId);
        log.info("Пользователь(id = {}) поставил лайк отзыву c id: {} .", userId, reviewId);
    }

    public void addDislike(Long reviewId, Long userId) {
        checkReviewId(userId);
        checkUserId(userId);
        reviewStorage.addDislike(reviewId, userId);
        log.info("Пользователь(id = {}) поставил дизлайк отзыву c id: {} .", userId, reviewId);
    }

    public void removeLike(Long reviewId, Long userId) {
        checkReviewId(userId);
        checkUserId(userId);
        reviewStorage.removeLike(reviewId, userId);
        log.info("Пользователь(id = {}) удалил лайк отзыву c id: {} .", userId, reviewId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        checkReviewId(userId);
        checkUserId(userId);
        reviewStorage.removeDislike(reviewId, userId);
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
