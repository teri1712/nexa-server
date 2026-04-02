package com.decade.nexa.users.application.ports.in;

import com.decade.nexa.users.dto.AccountResponse;
import com.decade.nexa.users.dto.ProfileRequest;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.SignUpRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public interface ProfileService {

      ProfileResponse createIfNotExists(SignUpRequest signUpRequest, boolean usernameAsIdentifier) throws DataIntegrityViolationException;

      ProfileResponse create(SignUpRequest signUpRequest, boolean usernameAsIdentifier) throws DataIntegrityViolationException;

      ProfileResponse changeProfile(UUID id, ProfileRequest profileRequest);

      AccountResponse changePassword(UUID id, String newPassword, String password) throws AccessDeniedException;

      ProfileResponse findById(UUID id);

      ProfileResponse findByUsername(String username);

}
