# Repository Guidelines

## Project Structure & Module Organization

This is a Spring Boot 3.4/Maven application using Java 21, Thymeleaf, Spring Security, JPA, and MySQL.

- `src/main/java/xyz/subho/retail/banking/` contains `controller`, `service`, `service/serviceImpl`, `dao`, `model`, `security`, and `config` packages. `Application.java` is the entry point.
- `src/test/java/xyz/subho/retail/banking/` contains JUnit tests for services, security, and application context.
- `src/main/resources/templates/` contains Thymeleaf views; `static/` contains CSS, JavaScript, and images.
- `src/main/resources/db/changelog/` contains Liquibase migrations. Add schema changes there when entities change.
- `.github/workflows/` contains Maven verification, Sonar, and container-publishing workflows. Docker Compose files define development and production-like environments.

## Build, Test, and Development Commands

- `mvn clean verify` — compile, run tests, generate JaCoCo output, and verify the build. Use `./mvnw` or `.\mvnw.cmd` when Maven is not installed.
- `docker compose -f docker-compose.dev.yml up -d` — start the development MySQL database; run `Application` from the IDE with the `dev` profile.
- `docker compose -f docker-compose.prod.yml up -d --build` — run the production-like Compose setup.
- `mvn liquibase:diff -Dliquibase.diffChangeLogFile=src/main/resources/db/changelog/changes/<version>-<description>.yaml` — generate a migration after entity changes.

## Coding Style & Naming Conventions

Use standard Java style: 4 spaces, Unix line endings, explicit imports, camelCase methods/variables, and PascalCase classes. Keep Spring layers in their existing packages and name implementations with the `Impl` suffix. Add Javadoc to public classes and methods.

## Testing Guidelines

Tests use JUnit 5, Spring Boot Test, Spring Security Test, Mockito, and Testcontainers MySQL. Name tests after the class under test with the `Test` suffix, keep them under the matching package, and add coverage for new behavior and security-sensitive changes. Docker must be available for the full Testcontainers suite.

## Commit & Pull Request Guidelines

Use short imperative commit subjects, matching repository history (for example, `Update CI workflow` or `Refactor account service`). Pull requests should explain the change, link the relevant issue, include passing `mvn clean verify` output, update documentation and migrations when needed, and include screenshots for UI changes.

## Security & Configuration

Never commit credentials or production secrets. Review `SecurityConfig` and `RequestFilter` when changing authentication, authorization, sessions, CORS, or public routes; add regression tests for those changes. Keep environment-specific values in the appropriate `application-*.properties` file or environment variables.
