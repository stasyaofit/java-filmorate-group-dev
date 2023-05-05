package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Builder
@Data
public class Film {
    private int id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Length(max = 200, message = "Максимальная длина описания — 200 символов")
    @NotBlank(message = "Название не может быть пустым")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;
}
