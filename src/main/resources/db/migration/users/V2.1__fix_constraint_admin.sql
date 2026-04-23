alter table admin
    drop constraint fk_created_by_id;
alter table admin
    add constraint fk_created_by_id foreign key (created_by_id) references admin (id);