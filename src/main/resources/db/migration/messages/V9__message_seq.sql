alter sequence messages_seq rename to message_seq;

alter table agent_messages
    rename to bot_messages;