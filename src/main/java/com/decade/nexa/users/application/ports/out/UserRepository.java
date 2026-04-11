package com.decade.nexa.users.application.ports.out;

import com.decade.nexa.users.api.UserInfo;
import com.decade.nexa.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

      Optional<User> findByUsername(String username);

      List<UserInfo> findByIdIn(Set<UUID> ids);

}