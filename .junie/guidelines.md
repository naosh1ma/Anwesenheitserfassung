# Development Guidelines for Erfassung Project

This document provides essential information for developers working on the Erfassung project.

## Build/Configuration Instructions

### Prerequisites
- Java 23 (or compatible version)
- Maven 3.8+ 
- MariaDB 10.x

### Database Setup
1. Ensure MariaDB is running on localhost:3306
2. Create a database named `anwesenheit`
3. Create a user with the following credentials:
   - Username: `admin_db`
   - Password: `M6950evajo`
   - Grant all privileges on the `anwesenheit` database to this user

### Building the Project
1. Clone the repository
2. Navigate to the project root directory
3. Run the following command to build the project:
   ```
   mvn clean install
   ```

### Running the Application
1. After building, run the application using:
   ```
   mvn spring-boot:run
   ```
2. The application will be available at `http://localhost:8080`

## Testing Information

### Test Framework
The project uses:
- JUnit 5 for unit testing
- Mockito for mocking dependencies
- Spring Boot Test for integration testing

### Running Tests
1. Run all tests using:
   ```
   mvn test
   ```
2. Run a specific test class using:
   ```
   mvn test -Dtest=ClassName
   ```
3. Run a specific test method using:
   ```
   mvn test -Dtest=ClassName#methodName
   ```

### Adding New Tests
1. Create test classes in the `src/test/java/com/art/erfassung/tests` directory
2. Follow the naming convention: `*Test.java` for test classes
3. Use appropriate annotations:
   - `@Test` for test methods
   - `@BeforeEach` for setup methods
   - `@Mock` for mock objects
   - `@MockBean` for Spring Boot test mocks

### Test Example
Here's a simple example of a service test using Mockito:

```java
package com.art.erfassung.tests;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.service.GruppeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GruppeServiceTest {

    @Mock
    private GruppeRepository gruppeRepository;

    private GruppeService gruppeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        gruppeService = new GruppeService(gruppeRepository);
    }

    @Test
    public void testFindAll() {
        // Arrange
        Gruppe gruppe1 = new Gruppe("Gruppe 1");
        gruppe1.setId(1);
        Gruppe gruppe2 = new Gruppe("Gruppe 2");
        gruppe2.setId(2);
        List<Gruppe> expectedGruppen = Arrays.asList(gruppe1, gruppe2);
        
        when(gruppeRepository.findAll()).thenReturn(expectedGruppen);

        // Act
        List<Gruppe> actualGruppen = gruppeService.findAll();

        // Assert
        assertEquals(expectedGruppen, actualGruppen);
        verify(gruppeRepository, times(1)).findAll();
    }
}
```

## Additional Development Information

### Project Structure
- `src/main/java/com/art/erfassung/controller` - REST and web controllers
- `src/main/java/com/art/erfassung/model` - JPA entity classes
- `src/main/java/com/art/erfassung/repository` - Spring Data JPA repositories
- `src/main/java/com/art/erfassung/service` - Business logic services
- `src/main/java/com/art/erfassung/dto` - Data Transfer Objects
- `src/main/resources/templates` - Thymeleaf templates
- `src/main/resources/static` - Static resources (CSS, JS, images)
- `src/test/java/com/art/erfassung/tests` - Test classes

### Code Style Guidelines
1. Follow standard Java naming conventions:
   - CamelCase for class names (e.g., `GruppeService`)
   - camelCase for method and variable names (e.g., `findOrThrow`)
   - ALL_CAPS for constants (e.g., `MAX_SIZE`)

2. Documentation:
   - Use JavaDoc comments for classes and public methods
   - Document parameters, return values, and exceptions
   - Include German descriptions where appropriate (as seen in existing code)

3. Exception Handling:
   - Use specific exceptions rather than generic ones
   - Document exceptions in JavaDoc comments
   - Use the global exception handler for controller-level exceptions

### Database Conventions
- Table names are in lowercase (e.g., `gruppe`)
- Primary keys are named `id`
- Foreign keys follow the pattern `entity_id`
- Use appropriate column types and constraints

### Logging
- Use SLF4J for logging
- Configure log levels in `application.properties`
- Current configuration sets root logging level to WARN

### Thymeleaf Templates
- Templates are in the `src/main/resources/templates` directory
- Follow the naming convention of the controllers
- Use Thymeleaf fragments for common elements