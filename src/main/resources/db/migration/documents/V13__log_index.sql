create table knowledge_log
(
    id        bigserial primary key,
    index_date date,
    status    varchar(255),
    message   varchar(255)
);


create unique index idx_knowledge_log_index_date on knowledge_log (index_date);