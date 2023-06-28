package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director createDirector(Director director) {
        director = directorStorage.createDirector(director);
        log.info("Добавлен режиссёр с: {}", director.getName());
        return director;
    }

    public Director updateDirector(Director director) {
        Integer id = director.getId();
        director = directorStorage.updateDirector(director);
        if (director == null) {
            throw new DirectorNotFoundException("Режиссёр с ID = " + id + " не найден.");
        }
        log.info("Обновлен режиссёр c id = {}", id);
        return director;
    }

    public void deleteDirector(Integer id) {
        if (!directorStorage.deleteDirector(id)) {
            throw new DirectorNotFoundException("Режиссёр с ID = " + id + " не найден.");
        }
        log.info("Удалён режиссёр c id = {} ", id);
    }

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(Integer id) {
        Director director = directorStorage.getDirector(id);
        if (director == null) {
            throw new DirectorNotFoundException("Режиссёр с ID = " + id + " не найден.");
        }
        return director;
    }
}
