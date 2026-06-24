---
name: spring-component-test
description: Comprehensive pattern for modular, dataset-driven integration testing in Spring Boot. Includes @ComponentTest meta-annotation, TestDataset lifecycles, and Testcontainers setup.
---

# Spring Component Testing Infrastructure

This pattern enables isolated, declarative integration testing with automatic dataset management. It uses meta-annotations to bundle common test configurations and provides a lifecycle for test data.

## Prerequisites

Add these dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<!-- Optional: for Event Publication Registry testing -->
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## 1. Test Containers Infrastructure

Create a `Containers.java` class to manage shared Testcontainers.

```java
@TestConfiguration
public class Containers {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }

    @Bean
    @ServiceConnection
    RedisContainer redis() {
        return new RedisContainer(DockerImageName.parse("redis:7-alpine"));
    }
}
```

## 2. Dataset Management Contract

### TestDataset.java
```java
public interface TestDataset {
    default void clean() {} // Called after each test method
    default void setup() {} // Called before each test method
}
```

## 3. Orchestration Logic

### DatasetImportSelector.java
Ensures the datasets passed to the annotation are registered as Spring beans.

```java
public class DatasetImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        var attributes = AnnotationAttributes.fromMap(
            metadata.getAnnotationAttributes(ComponentTest.class.getName()));
        if (attributes != null && attributes.containsKey("datasets")) {
            return Arrays.stream(attributes.getClassArray("datasets"))
                .map(Class::getName).toArray(String[]::new);
        }
        return new String[0];
    }
}
```

### DatasetTestExecutionListener.java
Hooks into the JUnit lifecycle to call `setup()` and `clean()`.

```java
public class DatasetTestExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestMethod(TestContext testContext) {
        getDatasets(testContext).forEach(TestDataset::setup);
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        getDatasets(testContext).forEach(TestDataset::clean);
    }

    private List<TestDataset> getDatasets(TestContext testContext) {
        ComponentTest annotation = MergedAnnotations.from(testContext.getTestClass())
            .get(ComponentTest.class).synthesize();
        ApplicationContext context = testContext.getApplicationContext();
        return Arrays.stream(annotation.datasets())
            .map(context::getBean).toList();
    }
}
```

## 4. The @ComponentTest Meta-Annotation

Bundles the container setup, import logic, and execution listener.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ActiveProfiles("test")
@Import({Containers.class, DatasetImportSelector.class})
@AutoConfigureMockMvc
@TestExecutionListeners(
    listeners = DatasetTestExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public @interface ComponentTest {
    Class<? extends TestDataset>[] datasets() default {};
}
```

## 5. Usage Example

### Implementation
```java
@TestComponent
@RequiredArgsConstructor
public class UserDataset implements TestDataset {
    private final UserRepository repository;
    @Override public void setup() { repository.save(new User("Alice")); }
    @Override public void clean() { repository.deleteAll(); }
}
```

### In a Test Class
```java
@ComponentTest(datasets = {UserDataset.class})
class UserApiTest {
    @Autowired MockMvc mockMvc;
    @Test
    void shouldFindAlice() {
        mockMvc.perform(get("/users/Alice")).andExpect(status().isOk());
    }
}
```

## Best Practices
- **Parallelism**: Using `ServiceConnection` and shared beans in `Containers.java` optimizes resource usage.
- **Dynamic Port Injection**: Use `DynamicPropertyRegistrar` for services that don't support `@ServiceConnection`.
- **Selective Datasets**: Only import the datasets required for the specific test to keep context small.
