package com.decade.nexa.users.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFactory {

    private final PasswordEncoder passwordEncoder;

    @Value("${super.admin.username}")
    private String superAdmin;

    @Value("${super.admin.password}")
    private String superPassword;

    public User createUser(String username, String name) {
        String password = passwordEncoder.encode(UUID.randomUUID().toString());
        return new User(UUID.randomUUID(), username, password, name, LocalDate.now(), 1.0f);
    }

    public Admin createAdmin(String username, String password, String name, LocalDate dob, Float gender, Optional<Admin> createdBy) throws NeedAParentAdminException {
        if (username.equals(superAdmin)) {
            if (!password.equals(superPassword)) {
                throw new AccessDeniedException("Super admin cannot be created due to mismatched credentials");
            }
        } else {
            if (createdBy.isEmpty()) {
                throw new NeedAParentAdminException(username);
            }
        }
        password = passwordEncoder.encode(password);
        return new Admin(UUID.randomUUID(), username, password, name, dob, gender, createdBy.orElse(null));
    }

}
