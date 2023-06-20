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

    private Boolean isPositive;

    private Long userId;

    private Long filmId;

    private Long useful;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("content", content);
        values.put("is_Positive", isPositive);
        values.put("user_Id", userId);
        values.put("film_Id", filmId);
        return values;
    }
}
