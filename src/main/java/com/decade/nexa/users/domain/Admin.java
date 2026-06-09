package com.decade.nexa.users.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Getter
public class Admin extends User {

    @ManyToOne
    private Admin createdBy;

    Admin(UUID id, String username, String password, String name, LocalDate dob, Float gender, Admin createdBy) {
        super(id, username, password, name, dob, gender);
        this.createdBy = createdBy;
        setRole(Role.ADMIN);
    }
}
