package com.decade.nexa.users.application.services;

import com.decade.nexa.common.security.UserClaims;
import com.decade.nexa.users.application.ports.in.SessionService;
import com.decade.nexa.users.application.ports.out.TokenGenerator;
import com.decade.nexa.users.application.ports.out.UserRepository;
import com.decade.nexa.users.domain.User;
import com.decade.nexa.users.dto.AccessToken;
import com.decade.nexa.users.dto.AccountResponse;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SessionServiceImpl implements SessionService {


    private final UserRepository users;
    private final TokenGenerator tokenGenerator;
    private final UserMapper userMapper;


    @Override
    public AccountResponse login(String username) {
        User user = users.findByUsername(username).orElseThrow();

        ProfileResponse profile = userMapper.map(user);
        UserClaims claims = new UserClaims(user.getId(), user.getUsername(), user.getName(), user.getRole().name());
        AccessToken credential = tokenGenerator.generate(claims);
        return new AccountResponse(profile, credential);
    }

}
