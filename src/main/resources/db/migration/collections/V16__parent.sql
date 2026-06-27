alter table collections
    add column parent_id bigint;
alter table collections
    add constraint fk_parent_id foreign key (parent_id) references collections (id);