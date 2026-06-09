create table knowledge_log
(
    id        uuid primary key,
    indexDate date,
    status    varchar(255),
    message   varchar(255)
);

create unique index idx_knowledge_log_indexDate on knowledge_log (indexDate);