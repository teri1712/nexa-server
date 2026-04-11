package com.decade.nexa.users.application.ports.in;

import com.decade.nexa.users.dto.ProfileRequest;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.SignUpRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public interface ProfileService {

      ProfileResponse create(SignUpRequest signUpRequest, UUID caller) throws DataIntegrityViolationException;

      ProfileResponse changeProfile(UUID id, ProfileRequest profileRequest);

      ProfileResponse changePassword(UUID id, String newPassword, String password) throws AccessDeniedException;

      ProfileResponse findByUsername(String username);

}
