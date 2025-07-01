```text
You are an expert senior Java backend developer specializing in building robust and secure microservices. Your task is to generate the complete code for a Spring Boot microservice named siae-auth.
Please adhere strictly to the following requirements, using the provided context from the attached files: auth数据表结构.md, auth_db.sql, and auth-service-api.md.
1. Project Setup & Core Technologies:
Framework: Spring Boot 3.2.x
Language: Java 17
Build Tool: Maven
Core Dependencies:
spring-boot-starter-web
spring-boot-starter-security
mybatis-plus-boot-starter (version 3.5.x)
mysql-connector-j
jjwt-api, jjwt-impl, jjwt-jackson (for JWT handling)
lombok
springdoc-openapi-starter-webmvc-ui (for API documentation)
Dependencies for Spring Cloud Alibaba Nacos (for service discovery and configuration).
2. Database & Data Layer:
The single source of truth for the database schema is the attached auth_db.sql file.
Generate all necessary MyBatis-Plus entity classes (e.g., Role, Permission, UserAuth, LoginLog, etc.) based on the tables defined in auth_db.sql. Use @TableName and other annotations as needed.
Generate the corresponding MyBatis-Plus Mapper interfaces for each entity.
3. API Layer (Controllers)
Implement all RESTful API endpoints exactly as specified in the attached auth-service-api.md file.
The base path for all APIs should be /api/v1/auth.
Use DTOs (Data Transfer Objects) for all request and response bodies to decouple the API layer from the domain entities. For example, create LoginRequest, LoginResponse, RoleCreateRequest, etc.
4. Security & Authentication Logic:
Spring Security Configuration (SecurityConfig):
Configure the application for stateless authentication (SessionCreationPolicy.STATELESS).
Disable CSRF.
Explicitly permit public access to /api/v1/auth/login and /api/v1/auth/token/refresh. All other endpoints must be authenticated.
Define a BCryptPasswordEncoder bean.
JWT Filter (JwtAuthenticationFilter):
Create a custom filter that executes once per request.
It must extract the JWT from the Authorization: Bearer <token> header.
Validate the token's signature, expiration, and claims.
If the token is valid, create an UsernamePasswordAuthenticationToken with the user's details and authorities, and set it in the SecurityContextHolder.
JWT Utility (JwtUtils):
Create a helper class to handle the creation and validation of JWTs.
Access Token Payload: Must include userId, username, and a claim named authorities containing a list of the user's permission codes (e.g., ["system:user:query", "system:role:add"]).
Refresh Token: A long-lived token used solely to get a new access token.
Login Endpoint Logic (/api/v1/auth/login):
This service must not validate passwords directly. It should make a Feign call to the siae-user service to get the user's details (including the hashed password) by username.
After fetching the user data, use BCryptPasswordEncoder to compare the request password with the hashed password.
On successful authentication:
Generate an Access Token and a Refresh Token.
Store the token pair and user ID in the user_auth table.
Asynchronously write a success record to the login_log table.
Return the token information as specified in the API document.
On authentication failure, asynchronously write a failure record to the login_log table and return an appropriate error response.
Logout Endpoint Logic (/api/v1/auth/logout):
This endpoint should invalidate the user's session by deleting their token record from the user_auth table based on the incoming valid Access Token.
5. RBAC & Authorization Logic:
Implement the full CRUD (Create, Read, Update, Delete) functionality for Role and Permission entities.
Implement the logic to manage the relationships in user_role and role_permission tables, as specified in the API document (e.g., assigning roles to a user, assigning permissions to a role).
6. Code Structure & Best Practices:
Use a standard layered architecture: controller, service, mapper, entity, dto, config, filter.
Implement a global exception handler (@RestControllerAdvice) to manage common exceptions like AuthenticationException, AccessDeniedException, validation errors, etc., and return a standardized JSON error response.
Use Lombok extensively to minimize boilerplate code.
Add clear Javadoc comments to public methods in the service and controller layers.
Please generate the complete, well-structured Maven project directory for the siae-auth module based on these instructions.
```