package com.decade.nexa.users.application.services;

import com.decade.nexa.users.application.ports.out.AdminRepository;
import com.decade.nexa.users.domain.Admin;
import com.decade.nexa.users.domain.UserFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SuperAdminSeeding implements ApplicationRunner {

      @Value("${super.admin.username}")
      private String superAdminUsername;

      @Value("${super.admin.password}")
      private String superAdminPassword;

      private final UserFactory userFactory;
      private final AdminRepository admins;

      @Override
      public void run(ApplicationArguments args) throws Exception {
            if (!admins.existsByUsername(superAdminUsername)) {
                  Admin admin = userFactory.createAdmin(superAdminUsername, superAdminPassword, "super admin", Instant.now(), 1.0f, Optional.empty());
                  admins.save(admin);
            }
      }
}
