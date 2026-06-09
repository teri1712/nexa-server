package com.decade.nexa.users;

import com.decade.nexa.common.TestDataset;
import com.decade.nexa.users.application.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;

import java.util.Set;

@RequiredArgsConstructor
@TestComponent
public class UserCleanUp implements TestDataset {

    private final UserRepository users;

    @Value("${super.admin.username}")
    private String superAdmin;


    @Override
    public void clean() {
        users.deleteByUsernameNotIn(Set.of(superAdmin));
    }
}
