package com.decade.nexa.messages.infra;

import com.decade.nexa.common.security.jwt.JwtService;
import com.decade.nexa.common.security.jwt.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

@Configuration
public class MessageSecurity {

    @Bean
    public SecurityFilterChain messageFilterChain(
        HttpSecurity http,
        JwtService jwtService
    ) throws Exception {
        http
            .securityMatcher("/messages/**", "/bot/**")
            .requestCache(Customizer.withDefaults())
            .securityContext(context ->
                context.securityContextRepository(new RequestAttributeSecurityContextRepository())
            )
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(Customizer.withDefaults())
            .exceptionHandling(exceptionHandling ->
                exceptionHandling.accessDeniedPage(null)
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .addFilterAfter(new JwtTokenFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize ->
                authorize.requestMatchers("/messages/**").authenticated()
                    .requestMatchers("/bot/**").permitAll()
            )
            .sessionManagement(session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
