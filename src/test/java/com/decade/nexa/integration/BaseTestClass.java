package com.decade.nexa.integration;


import com.decade.nexa.common.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Import({ContainerConfigs.class, OIDCConfig.class, AIEvalutationConfig.class, DataCleanUpBeans.class})
@AutoConfigureMockMvc
public class BaseTestClass {
      @Autowired
      private DataCleanUp data;

      @BeforeEach
      void cleanUp() {
            data.clean();
      }
}
