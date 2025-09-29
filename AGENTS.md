# Repository Guidelines

## Project Structure & Module Organization
The Spring Cloud backend lives under `siae/`, orchestrated by the root `pom.xml`. Shared foundations sit in `siae-core`, `siae-web-starter`, `siae-security-starter`, and the edge routing `siae-gateway`. Domain services reside in `services/` (`siae-auth`, `siae-user`, `siae-content`, `siae-message`), each following the standard `src/main/java` and `src/main/resources` layout with DTOs, controllers, and service implementations mirroring package names. The Vue 3 admin portal is isolated in `siae-admin/siae-admin-frontend`, where `src/views` hosts pages, `src/stores` contains Pinia modules, and `src/api` centralizes REST clients and interceptors. Supplemental design notes are collected in `markdown/` and deployment artifacts in `.vercel/`.

## Build, Test, and Development Commands
- `mvn clean install -DskipTests` (run in `siae/`): builds every Java module and refreshes shared jars.
- `mvn -pl services/siae-auth spring-boot:run` (replace module name as needed): boots an individual service with shared starters enabled.
- `mvn -pl services/siae-auth test`: executes the module's unit and integration suites.
- `npm install` then `npm run dev -- --host` in `siae-admin/siae-admin-frontend`: launches the admin SPA against local APIs.
- `npm run build`: outputs the production bundle to `dist/` for deployment.

## Coding Style & Naming Conventions
Prefer four-space indentation and `UTF-8` files in all Java modules. Keep classes in `UpperCamelCase`, methods and fields in `lowerCamelCase`, and REST endpoints grouped beneath `controller` packages. DTOs and response objects belong in `dto.request` and `dto.response`; enums and constants live in `enums` or `config`. Inject collaborators through Lombok's `@RequiredArgsConstructor` and keep Swagger annotations synchronized with request paths. Vue single-file components use PascalCase filenames (`UserList.vue`), composables stay lowerCamelCase (`useAuth.ts`), and shared types remain in `src/types`. Run IDE auto-formatting before committing.

## Testing Guidelines
Place backend tests under `src/test/java`, mirroring the production package. Leverage JUnit 5 with Spring Boot test slices; add `spring-boot-starter-test` as a test-scoped dependency within any module that gains new suites. Mock downstream services with Feign stubs or explicit `@MockBean` definitions, and store configuration fixtures under `src/test/resources`. For the admin portal, plan Vitest component tests under `src/__tests__` and wire them to upcoming CI once the script is added. Record manual smoke checks in each service's `issue.md` until automated coverage exists.

## Commit & Pull Request Guidelines
Follow the existing `type: 描述` pattern (`feat: ...`, `fix: ...`, `docs: ...`), keeping summaries concise and using numbered details for multi-part updates. Reference affected services and configuration files in the body. Pull requests should include a context-rich overview, linked tickets or `issue.md` references, screenshots or curl traces for new APIs, and confirmation that `mvn -pl <module> test` and `npm run build` succeed. Request reviews from the relevant module owners (`auth`, `user`, `content`, `gateway`) and call out follow-up tasks when deferring changes.
