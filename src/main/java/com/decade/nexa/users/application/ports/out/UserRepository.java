package com.decade.nexa.users.application.ports.out;

import com.decade.nexa.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

      Optional<User> findByUsername(String username);

      @Modifying
      @Transactional
      void deleteByUsernameNotIn(Set<String> usernames);

}