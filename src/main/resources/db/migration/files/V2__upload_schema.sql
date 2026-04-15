create type upload_progress as enum ('UPLOADING', 'COMPLETED');
create table upload_record
(
    id        varchar(255) primary key not null,
    file_name varchar(255)             not null,
    progress  upload_progress          not null,
    version   int
)