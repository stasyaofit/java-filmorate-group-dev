DROP ALL OBJECTS;

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

create table if not exists REVIEWS
(
    REVIEW_ID          BIGINT auto_increment,
    CONTENT CHARACTER VARYING(200) not null,
    FILM_ID BIGINT not null,
    USER_ID BIGINT not null,
    IS_POSITIVE BOOLEAN,
    constraint REVIEWS_PK
        primary key (REVIEW_ID),
    constraint "reviews_FILMS_FILM_ID_fk"
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint "reviews_USERS_USER_ID_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);

create table if not exists REVIEW_LIKES
(
    REVIEW_ID BIGINT not null,
    USER_ID BIGINT not null,
    LIKE_RATING BIGINT not null,
    constraint REVIEW_LIKES_PK
        primary key (REVIEW_ID, USER_ID),
    constraint "review_likes_REVIEWS_REVIEW_ID_fk"
        foreign key (REVIEW_ID) references REVIEWS ON DELETE CASCADE,
    constraint "review_likes_USERS_USER_ID_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);

create table if not exists FEED
(
    EVENT_ID   BIGINT auto_increment,
    ENTITY_ID  BIGINT     not null,
    USER_ID    BIGINT     not null,
    CREATED_TS TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    EVENT_TYPE varchar(6) not null,
    OPERATION  varchar(6) not null,
    constraint "FEED_pk"
        primary key (EVENT_ID),
    constraint "FEED_USERS_USER_ID_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);







