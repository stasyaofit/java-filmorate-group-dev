package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }
    public Collection<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    public Mpa getMpaById(Integer id) {
        checkMpaId(id);
        return mpaStorage.getMpa(id);
    }

    public void checkMpaId(Integer id) {
        if (id < 1 || mpaStorage.getMpa(id) == null) {
            throw new MpaNotFoundException("Рейтинг с ID = " + id + " не найден.");
        }
    }

}
