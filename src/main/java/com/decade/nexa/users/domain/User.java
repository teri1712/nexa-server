package com.decade.nexa.users.domain;

import com.decade.nexa.users.domain.events.UserCreated;
import com.decade.nexa.users.domain.events.UserPasswordChanged;
import com.decade.nexa.users.utils.GenderUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Table(name = "user_member")
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends AbstractAggregateRoot<User> {

    @Column(unique = true, nullable = false, updatable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String name;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    @Setter(AccessLevel.PACKAGE)
    private Role role;

    @Id
    private UUID id;

    @Version
    private Integer version;

    @Column(nullable = false)
    private Float gender;

    public void changeGender(@NotNull Float gender) {
        this.gender = gender;
    }

    void changePassword(@NotNull String password) {
        this.password = password;

        registerEvent(new UserPasswordChanged(username));
    }

    public void changeName(@NotNull String name) {
        this.name = name;
    }

    public void changeDob(@NotNull LocalDate dob) {
        this.dob = dob;
    }

    protected User() {
    }

    User(UUID id, String username, String password, String name, LocalDate dob, Float gender) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.setRole(Role.USER);
    }

    @PrePersist
    void onCreated() {
        registerEvent(new UserCreated(id, username, name, GenderUtils.inspect(gender), dob));
    }

}

