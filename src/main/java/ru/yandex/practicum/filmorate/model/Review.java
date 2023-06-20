package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Review {

    private Long reviewId;

    @Length(max = 200, message = "Максимальная длина описания — 200 символов")
    @NotBlank(message = "Название не может быть пустым")
    private String content;

    private boolean isPositive;

    @NotBlank(message = "Автор отзыва не может быть пустым")
    private Long userId;

    @NotBlank(message = "Фильм не может быть пустым")
    private Long filmId;

    private Long useful;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("reviewId", reviewId);
        values.put("content", content);
        values.put("isPositive", isPositive);
        values.put("userId", userId);
        values.put("filmId", filmId);
        values.put("useful", useful);
        return values;
    }
}
