package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review createReview(Review review);

    List<Review> getTopNReviews(Long count);

    List<Review> getTopNReviewsByFilmId(Long filmId, Long count);

    Review getReview(Long reviewId);

    Review updateReview(Review review);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void deleteReview(Long id);

    void removeLike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);
}
