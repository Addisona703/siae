# Repository Guidelines

## Project Structure & Module Organization
The Spring Cloud backend lives under `siae/`, orchestrated by the root `pom.xml`. Shared foundations sit in `siae-core`, `siae-web-starter`, `siae-security-starter`, and the edge routing `siae-gateway`. Domain services reside in `services/` (`siae-auth`, `siae-user`, `siae-content`, `siae-message`) with the standard `src/main/java` and `src/main/resources` layout, mirroring package names across DTOs, controllers, and service implementations. The Vue 3 admin portal is isolated in `siae-admin/siae-admin-frontend`, where `src/views` hosts pages, `src/stores` contains Pinia modules, and `src/api` centralizes REST clients and interceptors. Supplemental design notes live in `markdown/`, while deployment artifacts are stored in `.vercel/`.

## Build, Run, and Validation Commands
- `mvn clean install -DskipTests` (run in `siae/`): builds every Java module and refreshes shared jars.
- `mvn clean package -DskipTests` (run per module or in `siae/`): produces runnable Spring Boot jars without executing tests.
- `mvn -pl services/siae-<service> spring-boot:run`: boots an individual service with starters enabled.
- `mvn -pl services/siae-<service> test` or `mvn test`: executes unit and integration suites.
- `java -jar target/siae-<service>-*.jar --spring.profiles.active=dev`: runs a packaged service in dev mode.
- `java -jar target/*.jar --spring.config.location=classpath:/application-dev.yaml`: overrides configuration location.
- `npm install` then `npm run dev -- --host` in `siae-admin/siae-admin-frontend`: launches the admin SPA.
- `npm run build`: emits the production bundle to `dist/`.
- `curl http://localhost:{port}/api/v1/{service}/swagger-ui.html`: smoke-check running services (`auth` 8000, `content` 8010, `user` 8020, `message` 8030).

## Service Runtime & Dependencies
- Start Nacos (`startup.sh -m standalone`) alongside MySQL and Redis before launching services.
- Recommended startup order: Nacos/MySQL/Redis, then `siae-auth`, `siae-user`, `siae-content`, `siae-message`.
- Each service connects to its own MySQL schema and shares Redis for caching, JWT storage, and permission data.

## Database Operations
- Initialize databases manually by executing SQL scripts in order:
  1. `services/siae-auth/src/main/resources/sql/auth_db.sql`
  2. `services/siae-user/src/main/resources/sql/user_db.sql`
  3. `services/siae-content/src/main/resources/sql/content_db.sql`
  4. `services/siae-message/src/main/resources/sql/message_db.sql`

## Architecture Overview
- Java 17 Spring Boot 3.2.5 microservices with Spring Cloud 2023.0.1 and Spring Cloud Alibaba 2023.0.1.0.
- Service registry and configuration via Nacos; inter-service communication through OpenFeign; load balancing with Spring Cloud LoadBalancer.
- Persistence with MyBatis-Plus 3.5.6 plus Druid connection pooling; Redis powers caching and JWT permission storage.
- Security built on Spring Security + JWT, enriched by custom `@SiaeAuthorize` annotations and permission constants in `com.hngy.siae.core.permissions.*`.
- API documentation unified by SpringDoc OpenAPI 3 (Swagger) exposed per service at `/swagger-ui.html`.

## Coding Style & Naming Conventions
- Maintain four-space indentation and UTF-8 encoding for Java sources; keep classes in `UpperCamelCase`, methods and fields in `lowerCamelCase`.
- Group REST endpoints beneath `controller` packages; place DTOs under `dto.request` and `dto.response`, enums/constants under `enums` or `config`.
- Use Lombok `@RequiredArgsConstructor` for dependency injection and ensure Swagger annotations match documented paths.
- Vue SFCs use PascalCase filenames, composables remain lowerCamelCase, and shared types belong in `siae-admin/siae-admin-frontend/src/types`.
- Run IDE or formatter auto-formatting before committing.

## Development Standards
- Annotate every class JavaDoc with `@author KEYKB`.
- Use `@SiaeAuthorize` for permission checks (avoid `@PreAuthorize`); keep permission codes in the `module:resource:operation` format.
- Data modifications require `@Transactional(rollbackFor = Exception.class)`.
- Swagger usage limited to `@Tag`, `@Operation`, and `@Parameter`.
- Service interfaces extend `IService<T>`; service implementations extend `ServiceImpl<Mapper, Entity>` and use concise JavaDoc.
- Prefer `AssertUtils` for validation, returning error codes from `CommonResultCodeEnum`, `AuthResultCodeEnum`, or `UserResultCodeEnum`; add new codes where necessary.
- Utility choices: `StrUtil` for strings, `BeanConvertUtil` for object mapping, `PageConvertUtil` for pagination helpers.
- Controllers should apply `@Valid` to request bodies, explicitly name `@PathVariable` parameters, and supply Swagger `@Parameter` descriptions. Path prefixes stay in configuration; keep `@RequestMapping` clean.
- Entities map snake_case columns via `@TableField`; time fields appear as `createdAt` and `updatedAt` with automatic timestamps. Query DTOs use `createdAtStart` and `createdAtEnd`.
- Extract reusable business logic into private helpers and mark asynchronous work with `@Async` where appropriate.

## Security & JWT Notes
- JWT tokens contain minimal identity data (userId, username, expiration) while permissions are cached in Redis with TTL matching the token. Details in `markdown/JWT_OPTIMIZATION_GUIDE.md`.
- Supports role-based and direct user permissions; 82 of 99 APIs are permission-protected across 22 controllers.

## Testing Guidelines
- Mirror production packages under `src/test/java` and rely on JUnit 5 with Spring Boot test slices.
- Add `spring-boot-starter-test` as a test-scoped dependency when introducing new suites.
- Mock downstream integrations with Feign stubs or `@MockBean` definitions; keep fixtures in `src/test/resources`.
- For the admin portal, prepare Vitest suites under `siae-admin/siae-admin-frontend/src/__tests__` as CI scripting arrives.
- Record manual smoke checks in each service’s `issue.md` until automation covers scenarios.

## MCP Integration
- Filesystem MCP provides bulk read/write, search, and directory utilities (`read_multiple_files`, `edit_file`, `search_files`, etc.).
- Context7 MCP resolves library IDs and fetches documentation; call `resolve-library-id` before `get-library-docs` unless the ID is provided.
- GitHub MCP supports repository, issue, and pull-request operations (search, list, create, update). Prefer standard tools first, reserve MCP usage for advanced workflows or external documentation needs.

## Commit & Pull Request Guidelines
Follow the `type: 描述` pattern (`feat: ...`, `fix: ...`, `docs: ...`) with concise summaries. Reference impacted services and configuration entries, enumerate multi-part changes, and attach supporting evidence (screenshots, cURL traces, linked tickets or `issue.md`). Confirm `mvn -pl <module> test` and `npm run build` results before submission. Request reviews from module owners (`auth`, `user`, `content`, `gateway`) and list deferred follow-up tasks when applicable.
