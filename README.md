
# Spring Boot Accounts Transfer Application

This is a Spring Boot application designed to manage accounts and facilitate money transfers between them. The application uses Gradle as the build tool and is built with simplicity, thread safety, and scalability in mind. It also includes various features to make development, testing, and deployment more efficient.

## Overview

The **Accounts Transfer Application** provides the following functionality:
- Create new accounts with unique account IDs and positive initial balances.
- Retrieve details of an account by its ID.
- Transfer funds between two accounts, ensuring thread safety and avoiding deadlocks.
- Notify account holders of transfers through an email notification service.

The application is implemented with Spring Boot and in-memory data storage. It's designed to be a starting point that can be enhanced with various improvements.

## Building and Running the Application

### Prerequisites
- Java 17 or higher
- Gradle 7.6 or higher

### Build the Application
To build the application, run the following command in the project's root directory:
```bash
./gradlew build
```

### Run the Application
Start the application with the following command:
```bash
./gradlew bootRun
```
The application will run on port **18080** by default.

## REST API Endpoints

### 1. Create a New Account
- **Endpoint**: `POST /v1/accounts`
- **Description**: Creates a new account with an initial balance. The account ID must be unique.
- **Request Body**:
  ```json
  {
    "accountId": "12345",
    "balance": 1000.00
  }
  ```
- **Curl Command**:
  ```bash
  curl -X POST -H "Content-Type: application/json" -d '{"accountId": "12345", "balance": 1000.00}' http://localhost:18080/v1/accounts
  ```

### 2. Retrieve Account Information
- **Endpoint**: `GET /v1/accounts/{accountId}`
- **Description**: Fetches the details of an account by its ID.
- **Path Variable**: `accountId` - ID of the account to retrieve.
- **Curl Command**:
  ```bash
  curl -X GET http://localhost:18080/v1/accounts/12345
  ```

### 3. Transfer Money Between Accounts
- **Endpoint**: `POST /v1/accounts/transfer`
- **Description**: Transfers a specified amount from one account to another. Ensures that the amount is positive and the source account has sufficient funds.
- **Request Parameters**:
  - `accountFromId`: The ID of the account to transfer funds from.
  - `accountToId`: The ID of the account to transfer funds to.
  - `amount`: The amount to be transferred.
- **Curl Command**:
  ```bash
  curl -X POST "http://localhost:18080/v1/accounts/transfer?accountFromId=12345&accountToId=67890&amount=100"
  ```

## Future Improvements

### 1. Error Handling and Custom Exception Handling
   - Implement a global exception handler with `@ControllerAdvice` to provide consistent error responses.
   - Define custom exceptions for specific error scenarios (e.g., `InsufficientFundsException`, `AccountNotFoundException`).

### 2. DTOs and Data Transformation
   - Use Data Transfer Objects (DTOs) for request/response handling to separate from domain models and simplify future updates.
   - Leverage MapStruct or ModelMapper for automatic DTO mapping.

### 3. Transaction Management
   - Utilize Spring's `@Transactional` annotation for atomic operations, particularly in `transferMoney`, if a persistent database is used.

### 4. Concurrency Improvements
   - Consider using `ReadWriteLock` for better concurrency management in high-read scenarios.

### 5. Caching with Spring Cache
   - Implement caching for frequently accessed data using Spring’s caching abstraction and integrate with Redis or a similar caching solution.

### 6. Database Layer with JPA/Hibernate
   - Implement database interactions with JPA/Hibernate if moving to a persistent database, using repositories for data access and separation of concerns.

### 7. Observability with Logging and Monitoring
   - Enhance logging with SLF4J/Logback for structured and contextual logs.
   - Integrate monitoring with tools like Prometheus and Grafana.

### 8. Documentation and Developer Experience
   - Define API specifications with OpenAPI.
   - Integrate Swagger UI for interactive API documentation.
   - Include Postman or Insomnia collections for testing API endpoints.

### 9. Deployment Enhancements
   - Containerize the application with Docker.
   - Implement CI/CD pipelines using GitHub Actions, Jenkins, or GitLab CI.

### 10. Configuration Management
   - Use Spring Cloud Config for managing sensitive configuration properties.
   - Use environment-specific profiles for easy configuration management.

### 11. Security Enhancements
   - Integrate Spring Security for role-based access and JWT-based authentication.

## Additional Information

### Configuration
- **Port**: The server runs on port `18080` by default and can be configured in `application.properties`.

### Notification Service
- A simple notification service sends messages to account holders upon successful transfers, implemented as an email notification service.

### Testing
- Use `./gradlew test` to run unit tests. Integration tests are recommended to be added for better coverage.

### Dependencies
- **Spring Boot Starter Web**: For building RESTful APIs.
- **Spring Boot Starter Validation**: For validating input data.
- **Lombok**: Reduces boilerplate code for models and services.
- **JUnit**: For unit testing.
- **SLF4J/Logback**: For logging.

---

Feel free to add more details as you develop new features or make enhancements to the application. This README provides a solid foundation for understanding, building, and extending the application.
