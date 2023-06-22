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
        Integer id = directorStorage.createDirector(director).getId();
        log.info("Добавлен режиссёр с: {}", director.getName());
        return getDirectorById(id);
    }

    public Director updateDirector(Director director) {
        Integer id = director.getId();
        checkDirectorId(id);
        directorStorage.updateDirector(director);
        log.info("Обновлен режиссёр c id = {}", id);
        return director;
    }

    public void deleteDirector(Integer id) {
        checkDirectorId(id);
        directorStorage.deleteDirector(id);
    }

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(Integer id) {
        Director director = directorStorage.getDirector(id);
        if (director == null) {
            throw new DirNotFoundException("Режиссёр с ID = " + id + " не найден.");
        }
        return director;
    }

    private void checkDirectorId(Integer id) {
        if (id < 1 || directorStorage.getDirector(id) == null) {
            throw new DirNotFoundException("Режиссёр с ID = " + id + " не найден.");
        }
    }
}
