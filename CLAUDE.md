# CLAUDE.md

This file provides Claude Code with essential information about this repository to enable effective assistance with development tasks.

## Commands

### Build and Development
```bash
# Build entire project
mvn clean package -DskipTests

# Build specific service
cd services/siae-auth && mvn clean package -DskipTests

# Run development mode
java -jar target/siae-auth-*.jar --spring.profiles.active=dev

# Run with specific configuration
java -jar target/*.jar --spring.config.location=classpath:/application-dev.yaml
```

### Testing and Validation
```bash
# Run all tests (when test framework is available)
mvn test

# Validate services are running
curl http://localhost:8000/api/v1/auth/swagger-ui.html
curl http://localhost:8020/api/v1/user/swagger-ui.html
curl http://localhost:8010/api/v1/content/swagger-ui.html
curl http://localhost:8030/api/v1/message/swagger-ui.html
```

### Database Operations
```bash
# Initialize databases (run SQL scripts manually)
# Execute scripts in order:
# - services/siae-auth/src/main/resources/sql/auth_db.sql
# - services/siae-user/src/main/resources/sql/user_db.sql
# - services/siae-content/src/main/resources/sql/content_db.sql
# - services/siae-message/src/main/resources/sql/message_db.sql
```

### Service Management
```bash
# Start Nacos (required before starting services)
startup.sh -m standalone

# Service startup order (recommended):
# 1. Start Nacos, MySQL, Redis
# 2. siae-auth (port 8000)
# 3. siae-user (port 8020)
# 4. siae-content (port 8010)
# 5. siae-message (port 8030)
```

## Architecture Overview

This is a Java 17 Spring Boot 3.2.5 microservices project using Spring Cloud for service orchestration.

### Core Architecture
- **Framework**: Spring Boot 3.2.5, Spring Cloud 2023.0.1, Spring Cloud Alibaba 2023.0.1.0
- **Service Registry**: Nacos for service discovery and configuration management
- **Database**: MySQL 8.0 with separate databases per service (auth_db, user_db, content_db, message_db)
- **ORM**: MyBatis-Plus 3.5.6 with Druid connection pooling
- **Security**: Spring Security + JWT with Redis caching for permissions
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Cache**: Redis for token storage and permission caching

### Module Structure
```
siae-parent/
├── siae-core/                   # Core shared utilities and entities
├── siae-web-starter/           # Web configuration starter
├── siae-security-starter/      # Security configuration starter
├── siae-gateway/               # API Gateway service
└── services/
    ├── siae-auth/              # Authentication service (port 8000)
    ├── siae-user/              # User management service (port 8020)
    ├── siae-content/           # Content management service (port 8010)
    └── siae-message/           # Messaging service (port 8030)
```

### Service Dependencies
- All services depend on `siae-core`, `siae-web-starter`, `siae-security-starter`
- Services use OpenFeign for inter-service communication
- Each service has its own database schema
- Redis is used across services for caching and permission storage

### Security Architecture
- JWT-based authentication with Redis permission caching for performance
- RBAC (Role-Based Access Control) model with hierarchical permissions
- Custom `@SiaeAuthorize` annotations for permission checking
- Permission constants defined in `com.hngy.siae.core.permissions.*`
- Permissions cached in Redis with TTL matching JWT expiration

### Database Design
- **Naming Convention**: snake_case for tables/columns, camelCase for Java entities
- **Time Fields**: `created_at`, `updated_at` with automatic timestamps
- **Primary Keys**: `id` (BIGINT AUTO_INCREMENT)
- **Foreign Keys**: `{table}_id` format
- **Per-Service Databases**: Each service has its own MySQL database

## Development Rules and Conventions

### Required Annotations
- **Author**: All classes must have `@author KEYKB` in JavaDoc
- **Permissions**: Use `@SiaeAuthorize` (never `@PreAuthorize`)
- **Transactions**: Use `@Transactional(rollbackFor = Exception.class)` for data modifications
- **Swagger**: Only use `@Tag`, `@Operation`, `@Parameter` (forbidden: `@ApiResponses`, `@Schema`)

### Service Layer Standards
- All service interfaces extend `IService<T>`
- All service implementations extend `ServiceImpl<Mapper, Entity>`
- Use `@RequiredArgsConstructor` for dependency injection
- All methods must have concise JavaDoc comments

### Error Handling and Validation
- Use `AssertUtils` for all parameter and business validation (never hardcoded strings)
- Error codes managed in enums: `CommonResultCodeEnum`, `AuthResultCodeEnum`, `UserResultCodeEnum`
- Add new error codes to appropriate enums when needed

### Utility Standards
- String operations: Use `cn.hutool.core.util.StrUtil` (never Spring StringUtils)
- Object conversion: Use `BeanConvertUtil.to()` and `BeanConvertUtil.toList()`
- Pagination: Use `PageConvertUtil` for MyBatis-Plus page conversions

### Controller Standards
- Use `@Valid` for request body validation
- Path variables: `@PathVariable` with explicit names
- Parameters: `@Parameter` descriptions for Swagger
- Base paths managed in application.yaml, use `@RequestMapping` without prefixes

### Database and Entity Standards
- Entity time fields: `createdAt`, `updatedAt` with `@TableField` mapping to snake_case
- Query DTOs: Time range fields as `createdAtStart`, `createdAtEnd`
- Use MyBatis-Plus base methods: `saveBatch()`, `removeByIds()`, `listByIds()`

### Code Organization
- Extract repeated business logic into private methods
- Use `@Async` for non-critical operations like logging
- Clear method names that express intent
- Maintain consistent indentation and formatting

## Important Implementation Details

### JWT Optimization
The system uses optimized JWT tokens that only contain basic info (userId, username, exp). Full permissions are cached in Redis for performance. See `markdown/JWT_OPTIMIZATION_GUIDE.md` for details.

### Permission System
- Permission codes format: `module:resource:operation` (e.g., `content:article:create`)
- Constants defined in `AuthPermissions`, `ContentPermissions`, `UserPermissions`
- Cached in Redis with automatic expiration
- Supports both role-based and direct user permissions

### Service Communication
- Uses OpenFeign for synchronous service calls
- Service discovery via Nacos
- Load balancing with Spring Cloud LoadBalancer
- Circuit breaker patterns (when configured)

### Configuration Management
- Nacos Config for centralized configuration
- Profile-specific configs: `application-dev.yaml`, `application-prod.yaml`
- Group: `SIAE_GROUP` in Nacos
- Bootstrap configuration for early Nacos connection

### API Documentation
- Unified Swagger configuration in `siae-web-starter`
- Service-specific docs available at each service's `/swagger-ui.html`
- Total: 99 APIs across 22 controllers
- Permission-protected: 82 APIs, Public: 17 APIs

### Testing and Quality
- API testing via service-specific Swagger UIs
- Permission testing through dedicated test endpoints
- Database integration testing with each service's SQL scripts
- Redis connectivity testing for caching functionality

## File Structure Notes

- Main configuration in root `pom.xml` with version management
- Service-specific configs in each `services/*/pom.xml`
- SQL initialization scripts in `services/*/src/main/resources/sql/`
- Shared utilities and constants in `siae-core/`
- Security configurations in `siae-security-starter/`
- Web configurations in `siae-web-starter/`
- Project documentation in `markdown/` directory
- Development rules in `.augment/rules/siae-rule.md`

## MCP (Model Context Protocol) Integration

This project has integrated 3 MCP servers to enhance development capabilities. Use these tools when appropriate:

### 1. Filesystem MCP (`mcp__filesystem__*`)
**When to use:**
- Reading/writing files when standard tools are insufficient
- Batch file operations (e.g., `read_multiple_files` for reading several files at once)
- Directory operations like listing with sizes, creating directories, moving files
- Searching files with patterns and exclusions

**Available tools:**
- `mcp__filesystem__read_text_file`: Read complete file contents
- `mcp__filesystem__read_multiple_files`: Read multiple files simultaneously (more efficient than individual reads)
- `mcp__filesystem__write_file`: Create or overwrite files
- `mcp__filesystem__edit_file`: Make line-based edits with git-style diff preview
- `mcp__filesystem__list_directory`: List directory contents with [FILE]/[DIR] prefixes
- `mcp__filesystem__directory_tree`: Get recursive tree view as JSON
- `mcp__filesystem__search_files`: Recursively search for files by pattern
- `mcp__filesystem__get_file_info`: Get detailed file metadata (size, timestamps, permissions)
- `mcp__filesystem__move_file`: Move or rename files

**Example scenarios:**
- Reading multiple configuration files at once: Use `read_multiple_files` instead of multiple `Read` calls
- Exploring project structure: Use `directory_tree` for hierarchical view
- Finding files by name: Use `search_files` with patterns and exclusions

### 2. Context7 MCP (`mcp__context7-mcp__*`)
**When to use:**
- Fetching up-to-date documentation for external libraries and frameworks
- Resolving library names to Context7-compatible IDs
- Getting specific documentation topics (e.g., "hooks", "routing")

**Available tools:**
- `mcp__context7-mcp__resolve-library-id`: Convert library name to Context7 ID (e.g., "react" → "/facebook/react")
- `mcp__context7-mcp__get-library-docs`: Fetch documentation for a library using its Context7 ID

**Example scenarios:**
- User asks: "How do I use Spring Cloud Gateway filters?"
  1. Call `resolve-library-id` with libraryName="spring-cloud-gateway"
  2. Call `get-library-docs` with the resolved ID and topic="filters"
- Need documentation for Netty WebSocket handlers
  1. Call `resolve-library-id` with libraryName="netty"
  2. Call `get-library-docs` with context7CompatibleLibraryID and topic="websocket"

### 3. GitHub MCP (`mcp__github__*`)
**When to use:**
- Searching GitHub repositories, code, users, or issues
- Managing issues and pull requests
- Working with repository files and branches
- Creating or forking repositories

**Available tools:**
- **Search**: `search_repositories`, `search_code`, `search_users`, `search_issues`
- **Issues**: `get_issue`, `create_issue`, `update_issue`, `list_issues`, `add_issue_comment`, `get_issue_comments`
- **Pull Requests**: `create_pull_request`, `get_pull_request`, `list_pull_requests`, `merge_pull_request`, `get_pull_request_files`, `get_pull_request_comments`
- **Repository**: `get_repository`, `create_repository`, `fork_repository`, `get_file_contents`, `create_or_update_file`
- **Git Operations**: `list_commits`, `get_commit`, `list_branches`, `create_branch`, `list_tags`, `get_tag`, `push_files`

**Example scenarios:**
- User asks: "Find examples of Netty WebSocket implementations on GitHub"
  - Use `search_code` with q="WebSocket language:java NettyWebSocketServer"
- Need to check dependencies in a GitHub project
  - Use `get_repository` to get README and file structure
  - Use `get_file_contents` with path="pom.xml" to check Maven dependencies
- Creating issues or PRs after making changes
  - Use `create_issue` or `create_pull_request` with appropriate parameters

### MCP Usage Guidelines:
1. **Prefer standard tools first**: Use built-in Read, Write, Edit, Glob, Grep tools for common operations
2. **Use MCP for advanced scenarios**: Batch operations, external documentation, GitHub integration
3. **Combine tools efficiently**: Use `read_multiple_files` when you need several files, not individual reads
4. **Check documentation**: Use Context7 MCP when uncertain about library APIs or best practices
5. **GitHub operations**: Use GitHub MCP for repository exploration, code search, and collaboration tasks