
-- auto-generated definition
create type name_type as
(
    given_name text,
    sur_name   text
);

alter type name_type owner to postgres;



create table thing
(
    id uuid not null
        constraint thing_pk
            primary key
);

alter table thing
    owner to postgres;

create table person
(
    id            uuid not null
        primary key
        references thing,
    name          text,
    separate_name name_type,
    birth_date    date
);

alter table person
    owner to postgres;

create table organization
(
    id   uuid not null
        primary key
        references thing,
    name text
);

alter table organization
    owner to postgres;

