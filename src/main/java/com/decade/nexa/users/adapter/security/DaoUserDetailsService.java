package com.decade.nexa.users.adapter.security;

import com.decade.nexa.users.application.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DaoUserDetailsService implements UserDetailsService {

      private final UserRepository users;

      @Override
      public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            try {
                  return new DaoUser(users.findByUsername(username).orElseThrow());
            } catch (NoSuchElementException ex) {
                  log.warn("User not found {}", username);
                  throw new UsernameNotFoundException("Credential with Username: " + username + " does not exist.");
            }
      }
}
