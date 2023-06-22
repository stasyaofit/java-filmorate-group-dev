package ru.yandex.practicum.filmorate.exception;

public class DirNotFoundException extends RuntimeException {
    public DirNotFoundException(String s) {
        super(s);
    }
}