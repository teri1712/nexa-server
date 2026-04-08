package com.decade.nexa.users.application.services;

import com.decade.nexa.users.application.ports.in.ProfileService;
import com.decade.nexa.users.application.ports.out.AdminRepository;
import com.decade.nexa.users.application.ports.out.TokenStore;
import com.decade.nexa.users.application.ports.out.UserRepository;
import com.decade.nexa.users.domain.Admin;
import com.decade.nexa.users.domain.UserFactory;
import com.decade.nexa.users.domain.UserPasswordPolicy;
import com.decade.nexa.users.dto.ProfileRequest;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.SignUpRequest;
import com.decade.nexa.users.dto.mapper.UserMapper;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

      private final UserFactory userFactory;
      private final UserRepository users;
      private final AdminRepository admins;
      private final TokenStore tokens;
      private final UserMapper userMapper;

      private final UserPasswordPolicy passwordPolicy;


      @Override
      public ProfileResponse create(SignUpRequest signUpRequest, UUID caller) throws DataIntegrityViolationException {

            String username = signUpRequest.getUsername();
            String password = signUpRequest.getPassword();
            String name = signUpRequest.getName();
            Float gender = signUpRequest.getGender();
            Instant dob = signUpRequest.getDob();
            Optional<Admin> callerAdmin = admins.findById(caller);
            Admin admin = userFactory.createAdmin(username, password, name, dob, gender, callerAdmin);
            admins.save(admin);

            return userMapper.map(admin);
      }

      @Override
      public ProfileResponse changeProfile(UUID id, ProfileRequest profileRequest) throws OptimisticLockException {
            Admin admin = admins.findById(id).orElseThrow();
            if (profileRequest.getName() != null)
                  admin.changeName(profileRequest.getName());
            if (profileRequest.getDob() != null)
                  admin.changeDob(profileRequest.getDob());
            if (profileRequest.getGender() != null)
                  admin.changeGender(profileRequest.getGender());
            return userMapper.map(admin);
      }


      @Override
      public ProfileResponse changePassword(UUID id, String newPassword, String password) throws AccessDeniedException, OptimisticLockException {
            Admin admin = admins.findById(id).orElseThrow();

            passwordPolicy.change(admin, password, newPassword);

            users.save(admin);
            tokens.evict(admin.getUsername());

            return userMapper.map(admin);
      }

      @Override
      public ProfileResponse findByUsername(String username) {
            return users.findByUsername(username).map(userMapper::map).orElseThrow();
      }

}