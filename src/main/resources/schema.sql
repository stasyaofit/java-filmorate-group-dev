DROP ALL OBJECTS;

create table if not exists DIRECTOR
(
    DIRECTOR_ID   BIGINT auto_increment,
    DIRECTOR_NAME CHARACTER VARYING(50) not null,
    constraint "DIRECTOR_pk"
        primary key (DIRECTOR_ID)
);

create table if not exists USERS
(
    USER_ID  BIGINT auto_increment,
    EMAIL    CHARACTER VARYING(50) not null,
    LOGIN    CHARACTER VARYING(50) not null,
    NAME     CHARACTER VARYING(50),
    BIRTHDAY DATE                  not null,
    constraint USERS_PK
        primary key (USER_ID)
);

create table if not exists GENRES
(
    GENRE_ID   INTEGER auto_increment,
    GENRE_NAME CHARACTER VARYING(50) not null,
    constraint GENRES_PK
        primary key (GENRE_ID)
);

create table if not exists MPA_RATINGS
(
    RATING_ID   INTEGER auto_increment,
    RATING_NAME CHARACTER VARYING(10) not null,
    constraint MPA_RATINGS_PK
        primary key (RATING_ID)
);

create table if not exists FILMS
(
    FILM_ID          BIGINT auto_increment,
    FILM_NAME        CHARACTER VARYING(50)  not null,
    DESCRIPTION CHARACTER VARYING(200) not null,
    RELEASE_DATE     DATE                   not null,
    DURATION         INTEGER                not null,
    RATING_ID        INTEGER,
    constraint FILMS_PK
        primary key (FILM_ID),
    constraint "films_MPA_RATINGS_RATING_ID_fk"
        foreign key (RATING_ID) references MPA_RATINGS ON DELETE RESTRICT
);

create table if not exists FILM_DIRECTOR
(
    FILM_ID     BIGINT  not null,
    DIRECTOR_ID INTEGER not null,
    constraint "FILM_DIRECTOR_pk"
        primary key (FILM_ID, DIRECTOR_ID),
    constraint "FILM_DIRECTOR_DIRECTOR_DIRECTOR_ID_fk"
        foreign key (DIRECTOR_ID) references DIRECTOR ON DELETE CASCADE,
    constraint "FILM_DIRECTOR_FILMS_FILM_ID_fk"
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE
);

create table if not exists FILM_LIKES
(
    FILM_ID BIGINT not null,
    USER_ID BIGINT not null,
    constraint FILM_LIKES_PK
        primary key (FILM_ID, USER_ID),
    constraint "film_likes_FILMS_FILM_ID_fk"
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint "film_likes_USERS_USER_ID_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);

create table if not exists FILM_GENRES
(
    FILM_ID  BIGINT  not null,
    GENRE_ID INTEGER not null,
    constraint FILM_GENRES_PK
        primary key (FILM_ID, GENRE_ID),
    constraint "film_genres_FILMS_FILM_ID_fk"
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint "film_genres_GENRES_GENRE_ID_fk"
        foreign key (GENRE_ID) references GENRES ON DELETE RESTRICT
);

create table if not exists FRIENDS
(
    USER_ID   BIGINT not null,
    FRIEND_ID BIGINT not null,
    STATUS    BOOLEAN,
    constraint FRIENDS_PK
        primary key (USER_ID, FRIEND_ID),
    constraint "friends_USERS_USER_ID_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE,
    constraint "friends_USERS_USER_ID_fk2"
        foreign key (FRIEND_ID) references USERS ON DELETE CASCADE
);
