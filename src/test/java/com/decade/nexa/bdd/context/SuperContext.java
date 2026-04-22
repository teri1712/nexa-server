package com.decade.nexa.bdd.context;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@ScenarioScope
@Component
@Getter
public class SuperContext {
      @Value("${super.admin.username}")
      private String superUsername;
      @Value("${super.admin.password}")
      private String superPassword;
}
