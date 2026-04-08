package com.decade.nexa.users.application.ports.out;

import com.decade.nexa.users.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
      boolean existsByUsername(String username);
}
