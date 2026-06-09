create table cluster_log
(
    id          bigserial primary key,
    cluster_date date,
    status      varchar(255),
    message     varchar(255)
);

create unique index idx_cluster_log_date on cluster_log (cluster_date);
