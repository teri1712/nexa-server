create sequence messages_seq start with 1 increment by 50;
create table messages
(
    sequence_id  bigint primary key,
    message      text,
    created_at   timestamptz,
    message_type varchar(31)
);