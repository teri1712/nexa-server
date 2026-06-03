CREATE TABLE user_query
(
    id         bigserial primary key,
    query      text,
    created_at date
);

create index idx_query_creation on user_query (created_at);

create table faq
(
    cluster_id bigserial primary key,
    queries    text[],
    question   text,
    created_at date
);