alter table user_member
    alter column dob type date using dob::date;