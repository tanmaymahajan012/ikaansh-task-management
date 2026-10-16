# Secure Task Management Microservice

A RESTful task management backend built with Java 21, Spring Boot, Spring Security, JWT authentication, Spring Data JPA, and MySQL.

## Features

- User registration and login with JWT authentication.
- Password hashing using BCrypt.
- Create, retrieve, update, and delete tasks.
- Task ownership enforcement so users can access only their own tasks.
- Filter tasks by status, priority, and due date.
- Pagination and sorting for task listing.
- Input validation and centralized exception handling.
- Request logging and Swagger API documentation.
- Unit tests for task service functionality.

## Technologies Used

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- Maven
- JWT (JJWT)
- JUnit 5 and Mockito
- Swagger / OpenAPI

## Prerequisites

- Java 21 or later
- MySQL Server
- Git

## Configuration

Create a MySQL database named `task_management`.

Configure the following environment variables before running the application:

- `DB_USERNAME` — MySQL username
- `DB_PASSWORD` — MySQL password
- `JWT_SECRET` — Base64-encoded secret key

Never commit real passwords or secret keys to version control.

## Run the Application

On Windows PowerShell, from the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Ensure the required environment variables are configured in the same terminal before starting the application.

## Run Tests

```powershell
.\mvnw.cmd test
```

## API Documentation

After starting the application, open:

`http://localhost:8080/swagger-ui/index.html`

Use the JWT bearer token returned by the login endpoint to authorize protected API requests.

## Main API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Authenticate and obtain a JWT |
| POST | `/api/tasks` | Create a task |
| GET | `/api/tasks` | List tasks with filtering and pagination |
| GET | `/api/tasks/{id}` | Retrieve a task |
| PUT | `/api/tasks/{id}` | Update a task |
| DELETE | `/api/tasks/{id}` | Delete a task |

## Task Fields

Tasks support a title, description, status, priority, and due date.

- Status: `TODO`, `IN_PROGRESS`, `COMPLETED`
- Priority: `LOW`, `MEDIUM`, `HIGH`

## Database

The application uses MySQL with Hibernate/JPA schema management. The database schema is updated through the configured JPA setting.

## Security

Passwords are stored using BCrypt hashing. Protected endpoints require JWT authentication, and task operations are scoped to the authenticated user.

## License

Created as an internship assignment for evaluation.
