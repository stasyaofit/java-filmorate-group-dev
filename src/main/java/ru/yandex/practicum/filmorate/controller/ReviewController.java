package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        log.info("Получен POST-запрос к эндпоинту '/reviews' на добавление отзыва: {}.", review);
        return reviewService.createReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Получен PUT-запрос к эндпоинту '/reviews' на обновления отзыва с ID = {}", review.getReviewId());
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        log.info("Получен DELETE-запрос к эндпоинту: '/reviews' на удаление отзыва с ID={}", id);
        reviewService.deleteReview(id);
    }

    @GetMapping
    public Collection<Review> getReviews(
            @RequestParam(value = "filmId", required = false) Long filmId,
            @RequestParam(value = "count", required = false, defaultValue = "10") Long count) {
        if (filmId == null) {
            log.info("Получен GET-запрос к эндпоинту '/reviews' на получение {} отзывов.", count.toString());
            return reviewService.findTopNReviews(count);
        }
        log.info("Получен GET-запрос к эндпоинту '/reviews' на получение {} отзывов для фильма c id = {}.",
                count.toString(), filmId);
        return reviewService.findTopNReviewsByFilmId(filmId, count);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Получен GET-запрос к эндпоинту '/reviews/{id}' на получение отзывов с ID = {}.", id);
        return reviewService.getReviewById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен PUT-запрос к эндпоинту '/reviews/{id}/like/{userId}'." +
                " Пользователь с ID {} ставит лайк отзыву с ID {}.", userId, id);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен PUT-запрос к эндпоинту '/reviews/{id}/dislike/{userId}'." +
                " Пользователь с ID {} ставит дизлайк отзыву с ID {}.", userId, id);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту '/reviews/{id}/like/{userId}'." +
                " Пользователь с ID {} удаляет лайк отзыву с ID {}.", userId, id);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту '/reviews/{id}/like/{userId}'." +
                " Пользователь с ID {} удаляет дизлайк отзыву с ID {}.", userId, id);
        reviewService.removeDislike(id, userId);
    }
}
