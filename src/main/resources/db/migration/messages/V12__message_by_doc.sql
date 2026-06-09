alter table user_messages
    add column doc_id varchar(255);
alter table bot_messages
    add column doc_id varchar(255);

alter table bot_messages
    rename to answer_messages;
