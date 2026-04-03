package com.decade.nexa.bdd.context;

import com.decade.nexa.users.dto.ProfileResponse;
import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class SignUpContext {
      public int status;
      public String errorMessage;
      public ProfileResponse profile;
}
