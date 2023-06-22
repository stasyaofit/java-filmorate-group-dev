package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info("Получен POST-запрос к эндпоинту '/directors' на добавление режиссёра: {}.", director);
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Получен PUT-запрос к эндпоинту '/directors' на обновления режиссёра с ID = {}", director.getId());
        return directorService.updateDirector(director);
    }

    @GetMapping
    public Collection<Director> findAll() {
        log.info("Получен GET-запрос к эндпоинту '/directors' на получение списка всех режиссёров.");
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Integer id) {
        log.info("Получен GET-запрос к эндпоинту '/directors/{id}' на получение режиссёра с ID = {}.", id);
        return directorService.getDirectorById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Integer id) {
        log.info("Получен DELETE-запрос к эндпоинту: '/directors' на удаление режиссёра с ID={}", id);
        directorService.deleteDirector(id);
    }
}
