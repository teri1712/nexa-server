package com.decade.nexa.bdd.context;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class UploadContext {
      public int finishStatus;
      public String key;
      public String downloadUrl;
}
