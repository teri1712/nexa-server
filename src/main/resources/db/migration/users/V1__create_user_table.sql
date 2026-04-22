create table user_member
(
    id       uuid primary key,
    name     varchar(255),
    username varchar(255) unique,
    password varchar(255),
    role     varchar(20),
    gender   real,
    version  int,
    dob      timestamptz
);

create table admin
(
    id            uuid primary key,
    created_by_id uuid
);

alter table admin
    add constraint fk_admin_id foreign key (id) references user_member (id) on delete cascade;
alter table admin
    add constraint fk_created_by_id foreign key (id) references user_member (id);