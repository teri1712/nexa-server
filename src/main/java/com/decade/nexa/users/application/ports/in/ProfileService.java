package com.decade.nexa.users.application.ports.in;

import com.decade.nexa.users.domain.NeedAParentAdminException;
import com.decade.nexa.users.domain.UserAlreadyExistException;
import com.decade.nexa.users.domain.WrongPasswordException;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.SignUpRequest;

import java.util.UUID;

public interface ProfileService {

    ProfileResponse createAdmin(SignUpRequest signUpRequest, UUID caller) throws NeedAParentAdminException, UserAlreadyExistException;

    ProfileResponse createUser(String username, String name) throws UserAlreadyExistException;

    ProfileResponse changeAdminPassword(UUID id, String newPassword, String password) throws WrongPasswordException;

    ProfileResponse findByUsername(String username);

}
