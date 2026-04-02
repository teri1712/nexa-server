create table user_member
(
    id       uuid primary key,
    name     varchar(255),
    username varchar(255) unique,
    password varchar(255),
    role     varchar(20),
    avatar   varchar(255),
    gender   real,
    version  int,
    dob      timestamptz
);