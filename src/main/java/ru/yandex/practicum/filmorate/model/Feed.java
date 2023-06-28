package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Feed {
    private Long eventId;

    @NotNull(message = "Отсутствует entityId события")
    private Long entityId;

    @NotNull(message = "Отсутствует автор события")
    private Long userId;

    private Long timestamp;

    @NotNull(message = "Отсутствует тип события")
    private EventType eventType;

    @NotNull(message = "Отсутствует вид операции")
    private Operation operation;
}

