package com.decade.nexa.users.application.services;

import com.decade.nexa.users.application.ports.in.TokenSessionService;
import com.decade.nexa.users.application.ports.out.TokenGenerator;
import com.decade.nexa.users.application.ports.out.TokenStore;
import com.decade.nexa.users.application.ports.out.UserRepository;
import com.decade.nexa.users.domain.User;
import com.decade.nexa.users.domain.UserFactory;
import com.decade.nexa.users.dto.AccessToken;
import com.decade.nexa.users.dto.AccountResponse;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.mapper.UserMapper;
import com.decade.nexa.web.security.UserClaims;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class TokenSessionServiceImpl implements TokenSessionService {


      private final TokenStore tokenStore;
      private final UserFactory userFactory;
      private final UserRepository users;
      private final TokenGenerator tokenGenerator;
      private final UserMapper userMapper;


      private UserClaims validate(String refreshToken) throws AccessDeniedException {
            UserClaims claims;
            try {
                  claims = tokenGenerator.decode(refreshToken);
            } catch (JwtException e) {
                  throw new AccessDeniedException("Token expired", e);
            }
            if (!tokenStore.has(claims.username(), refreshToken)) {
                  throw new AccessDeniedException("Token expired");
            }
            return claims;
      }

      @Override
      public String refresh(String refreshToken) throws AccessDeniedException {
            UserClaims claims = validate(refreshToken);
            return tokenGenerator.generate(claims).accessToken();
      }

      @Override
      public void logout(String username, String refreshToken) {
            tokenStore.evict(username, refreshToken);
      }

      @Override
      public AccountResponse login(String username) {
            User user = users.findByUsername(username).orElseThrow();
            return createNewSession(user);
      }

      private AccountResponse createNewSession(User user) {
            ProfileResponse profile = userMapper.map(user);
            UserClaims claims = new UserClaims(user.getId(), user.getUsername(), user.getName(), user.getRole().name());
            AccessToken credential = tokenGenerator.generate(claims);
            tokenStore.add(user.getUsername(), credential.refreshToken());
            return new AccountResponse(profile, credential);
      }

      @Override
      public AccountResponse loginOauth(Jwt jwt) {

            String username = jwt.getSubject();

            var claims = jwt.getClaims();
            String name = claims.get("name").toString();

            User user = users.findByUsername(username).orElseGet(new Supplier<User>() {
                  @Override
                  public User get() {
                        String password = UUID.randomUUID().toString();
                        Float gender = new Random().nextFloat();
                        Instant dob = Instant.now();
                        User user = userFactory.createUser(username, password, name, dob, gender);
                        return users.saveAndFlush(user);

                  }
            });

            return createNewSession(user);


      }
}
