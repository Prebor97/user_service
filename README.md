# User Service

## Overview
The User Service is a core component of the ticket event-driven architecture, responsible for handling user authentication and role management. It serves as the central point for creating and assigning user roles, managing user profiles, and ensuring secure access to other services in the system. The User Service communicates with other services (Events, Payment, Reconciliation, and Notification) via Apache Kafka to ensure seamless interaction in the event-driven ecosystem.

## Functionality
The User Service provides the following key functionalities:
- **User Authentication**: Manages user login, registration, and authentication processes.
- **Role Management**: Creates and assigns three distinct roles (e.g., Admin, Customer, Organizer) to users, which are used to control access to various features across the system.
- **User Profile Management**: Stores and manages user profile information, such as name, email, and other relevant details.
- **Event-Driven Communication**: Publishes and consumes events via Kafka to interact with other services, such as notifying the Events Service of user-related actions or receiving updates from other services.

## Architecture
The User Service is a standalone application built to operate within a microservices architecture. It communicates asynchronously with other services using Kafka topics for event-driven interactions. The service is designed to be scalable, secure, and fault-tolerant.

### Database
The User Service uses a dedicated **User Database** with the following tables:
- **User Table**: Stores authentication-related information, such as user ID, username, password (hashed), and role assignments.
- **Profile Table**: Stores user profile details, such as name, email, phone number, and other personal information.

### Integration with Kafka
The User Service interacts with other services through Kafka topics:
- **Publishes**: Events such as user registration, role assignment, or profile updates to notify other services (e.g., Events Service or Notification Service).
- **Consumes**: Events from other services, such as payment status updates or event booking confirmations, to update user-related data or trigger further actions.

## Responsibilities
- **Authentication**: Validates user credentials and issues authentication tokens (e.g., JWT) for secure access to the system.
- **Role Assignment**: Assigns one of three roles (Admin, Customer, Organizer) to users, which determines their access to features in the Events Service and other parts of the system.
- **Profile Management**: Allows users to create, update, and retrieve their profile information.
- **Event Notifications**: Publishes events to Kafka for actions like user registration or role changes, which may trigger notifications via the Notification Service or updates in the Events Service.

## Dependencies
- **Apache Kafka**: For event-driven communication with other services (Events, Payment, Reconciliation, Notification).
- **User Database**: A relational database (e.g., MySQL, PostgreSQL) to store the User and Profile tables.
- **Authentication Library**: For secure authentication (e.g., Spring Security, Keycloak).
- **Programming Language/Frameworks**: The service can be implemented in a language like Java (with Spring Boot) or another suitable framework.

## Setup and Installation
1. **Clone the Repository and checkout to your branch**:
   ```bash
   git clone <repository-url>
   cd user-service
   git checkout -b feature/feature-name

2. **Configure Dependencies**:
- Ensure Kafka is running and accessible.
- Set up the User Database with the required schema for the User and Profile tables.
- Update configuration files (e.g., `application.properties` or `application.yml`) with database credentials and Kafka broker details. Use application.example.properties file as a guide to set your applications.properties.

3. **Build and Run**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run

4. **Environment Variables**
- `DB_URL`: Database connection URL.
- `DB_USERNAME`: Database username.
- `DB_PASSWORD`: Database password.
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker address (e.g., `localhost:9092`).

## API Endpoints
The User Service exposes RESTful APIs for authentication and profile management. Examples include:
- `POST /v1/api/auth/register`: Register a new user.
- `POST /v1/api/auth/login`: Authenticate a user and return a token.
- `DELETE /v1/api/users/{user_id}`: Delete user profile
- `POST /v1/api/auth/admin`: Creates an admin
- `POST /v1/api/auth/reset-password/request`: Request to reset password
- `POST /v1/api/auth/reset-password/confirm`: Confirms password reset
- `PUT /v1/api/auth/profiles/{user_id}`: Updates user profile
- `GET /v1/api/auth/users/{user_id}/info`: Gets user information
- `PUT /v1/api/auth/profiles/{user_id}`: role: Assign role to user

## Kafka Topics

### Published Topics
- `user-registered-topic`: Triggered when a new user is registered.
- `user-logged-in-topic`: Triggered when a user logs in.
- `user-deleted-topic`: Triggered when a user is deleted.
- `admin-created-topic`: Triggered when an admin is created.
- `password-reset-requested-topic`: Triggered when a user requests a password reset
- `user-role-updated-topic`: Triggered when a user's role is updated

### Published Events
- `UserRegisteredEvent`: Triggered when a new user is registered.
- `UserLoggedInEvent`: Triggered when a user logs in.
- `UserDeletedEvent`: Triggered when a user is deleted.
- `AdminCreatedEvent`: Triggered when an admin is created.
- `PasswordResetRequestedEvent`: Triggered when a user requests a password reset
- `UserRoleUpdatedEvent`: Triggered when a user's role is updated


## Scaling and Fault Tolerance
- The User Service is designed to be stateless, allowing horizontal scaling by deploying multiple instances.
- Kafka ensures reliable event delivery, and the service uses retries and error handling for fault tolerance.
- Database transactions are managed to ensure data consistency during user and profile updates.

## Security
- Passwords are hashed using a secure algorithm (e.g., bcrypt).
- Authentication tokens (e.g., JWT) are used to secure API endpoints.
- Role-based access control (RBAC) ensures users only access authorized features.

## Future Improvements
- Add support for OAuth2 or external identity providers.
- Implement caching (e.g., Redis) for frequently accessed user data.
- Enhance role management with more granular permissions.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue on GitHub for bugs, improvements, or feature requests.
