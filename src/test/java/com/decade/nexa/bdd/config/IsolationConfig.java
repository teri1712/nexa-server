package com.decade.nexa.bdd.config;

import com.decade.nexa.common.DataCleanUp;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class IsolationConfig {
      
      private final DataCleanUp data;

      @Before
      public void cleanUpDocs() {
            data.clean();
      }

}
