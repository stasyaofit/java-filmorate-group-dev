package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MpaStorage {
    Collection<Mpa> findAll();

    Mpa getMpa(Integer id);

    Map<Long, Mpa> getMpaMap(List<Long> ids);
}
