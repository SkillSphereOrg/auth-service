# SkillSphere Auth Service

A modern, production-grade authentication and authorization microservice for the SkillSphere platform.

## Features

- JWT authentication (login, registration)
- Role-based access control (RBAC) with Admin/User roles
- Admin endpoints for user management
- Swagger/OpenAPI documentation
- Seed/demo data (admin user, roles)
- Dockerized for easy deployment

## Tech Stack

- Java 21, Spring Boot 3
- Spring Security, Spring Data JPA
- MySQL
- JWT (io.jsonwebtoken)
- Swagger/OpenAPI (springdoc-openapi)
- Docker

## Setup & Running Locally

### Prerequisites

- Java 21+
- MySQL running (update credentials in `src/main/resources/application.properties`)
- Gradle (or use the included wrapper)

### Build & Run

```sh
./gradlew bootRun
```

### Docker

Build and run the service in a container:

```sh
docker build -t skillsphere-auth-service .
docker run -p 8080:8080 --env-file .env skillsphere-auth-service
```

## API Documentation

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Demo Accounts

- **Admin:**
  - Username: `admin`
  - Password: `admin123`
- **Register new users** via `/api/auth/register`

## Example Endpoints

- `POST /api/auth/register` — Register a new user
- `POST /api/auth/login` — Login and receive JWT
- `GET /api/auth/me` — Get current user profile (JWT required)
- `GET /api/admin/users` — List all users (admin only)
- `PUT /api/admin/user/{id}/roles` — Assign roles to user (admin only)

## Environment Variables

You can use a `.env` file or environment variables to override DB credentials, JWT secret, etc.

## Screenshots

_Add screenshots of Swagger UI or Postman here_

## License

[MIT](LICENSE)
