create table user_messages
(
    sequence_id bigint primary key,
    content     text not null,
    created_at  timestamptz,
    user_id     uuid references user_member (id)
);

create table agent_messages
(
    sequence_id     bigint primary key,
    content         text,
    created_at      timestamptz,
    user_id         uuid references user_member (id),
    user_message_id bigint references user_messages (sequence_id)
);

insert into user_messages
select sequence_id, content, created_at, user_id
from messages
where message_type = 'user';


create index user_messages_seq_index on user_messages (user_id, sequence_id);
create index agent_messages_seq_index on agent_messages (user_id, sequence_id);

insert into agent_messages
select m.sequence_id,
       m.content,
       m.created_at,
       m.user_id,
       (select um.sequence_id
        from user_messages um
        where um.user_id = m.user_id
          and um.sequence_id < m.sequence_id
        order by um.sequence_id desc
        limit 1) as user_message_id
from messages m
where message_type = 'agent';