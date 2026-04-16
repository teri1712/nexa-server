package com.decade.nexa.users.application.ports.in;

import com.decade.nexa.users.domain.NeedAParentAdminException;
import com.decade.nexa.users.domain.UserAlreadyExistException;
import com.decade.nexa.users.domain.WrongPasswordException;
import com.decade.nexa.users.dto.ProfileRequest;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.SignUpRequest;

import java.util.UUID;

public interface ProfileService {

      ProfileResponse create(SignUpRequest signUpRequest, UUID caller) throws NeedAParentAdminException, UserAlreadyExistException;

      ProfileResponse changeProfile(UUID id, ProfileRequest profileRequest);

      ProfileResponse changePassword(UUID id, String newPassword, String password) throws WrongPasswordException;

      ProfileResponse findByUsername(String username);

}
