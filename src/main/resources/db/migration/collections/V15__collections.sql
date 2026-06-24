CREATE TABLE collections
(
    id      BIGSERIAL PRIMARY KEY,
    user_id UUID         NOT NULL,
    name    VARCHAR(255) NOT NULL
);

CREATE TABLE collection_items
(
    id            BIGSERIAL PRIMARY KEY,
    collection_id BIGINT       NOT NULL,
    document_id   VARCHAR(255) NOT NULL,
    added_at      DATE         NOT NULL
);
