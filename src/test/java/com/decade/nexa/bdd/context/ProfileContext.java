package com.decade.nexa.bdd.context;

import com.decade.nexa.users.dto.AccessToken;
import com.decade.nexa.users.dto.ProfileResponse;
import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class ProfileContext {
      public ProfileResponse profile;
      public AccessToken accessToken;
}
