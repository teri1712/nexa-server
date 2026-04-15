package com.decade.nexa.actuator.infras;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurity {
      @Value("${actuator.user.name}")
      private String actuatorUsername;

      @Value("${actuator.user.password}")
      private String actuatorPassword;

      @Value("${actuator.user.roles}")
      private String actuatorRoles;

      @Bean
      @Order(0)
      public SecurityFilterChain myActuatorSecurity(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
            UserDetails actuator = User.builder()
                      .username(actuatorUsername)
                      .password(passwordEncoder.encode(actuatorPassword))
                      .roles(actuatorRoles.split(","))
                      .build();
            DaoAuthenticationProvider adminProvider = new DaoAuthenticationProvider(new InMemoryUserDetailsManager(actuator));
            adminProvider.setPasswordEncoder(passwordEncoder);

            http
                      .authenticationManager(new ProviderManager(adminProvider))
                      .securityMatcher(EndpointRequest.toAnyEndpoint())
                      .authorizeHttpRequests(
                                auth -> auth
                                          .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("OPS")
                      )
                      .httpBasic(Customizer.withDefaults())
                      .csrf(AbstractHttpConfigurer::disable);
            return http.build();
      }

}
