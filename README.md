# Retail Banking Application

![Java CI with Maven](https://github.com/ohbus/retail-banking/workflows/Java%20CI%20with%20Maven/badge.svg?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ohbus_retail-banking&metric=alert_status)](https://sonarcloud.io/dashboard?id=ohbus_retail-banking)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=ohbus_retail-banking&metric=ncloc)](https://sonarcloud.io/dashboard?id=ohbus_retail-banking)
[![Docker pulls](https://img.shields.io/docker/pulls/subhrodip/retail-banking)](https://hub.docker.com/r/subhrodip/retail-banking)

A full-featured retail banking application built with Spring Boot, Thymeleaf, and Spring Security. This project simulates core banking functionalities, including user authentication, account management, and fund transfers.

The application is live at [bank.subho.xyz](https://bank.subho.xyz)

## Features

*   **User Management:** Secure user registration and login with Spring Security.
*   **Account Management:** View and manage savings and checking accounts.
*   **Fund Transfers:** Transfer funds between own accounts or to other recipients.
*   **Transaction History:** View a detailed history of all transactions.
*   **Admin Panel:** Manage users and view system-wide activity.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

*   Docker (Docker Compose is now included with Docker Desktop and Docker Engine; no separate installation is needed)

### Installation

#### Option 1: Clone the repository (recommended)
1.  **Clone the repository:**
    ```sh
    git clone https://github.com/ohbus/retail-banking.git
    ```
2.  **Navigate to the project directory:**
    ```sh
    cd retail-banking
    ```

#### Option 2: Download as ZIP (for beginners or those without a GitHub account)
1.  Go to the [GitHub repository page](https://github.com/ohbus/retail-banking).
2.  Click the green **Code** button, then select **Download ZIP**.
3.  Extract the ZIP file to your desired location.
4.  Open a terminal and navigate to the extracted `retail-banking` directory.

## Development

To start the application in a development environment, you can use the provided Docker Compose file to start a local MySQL database.

1.  **Start the development database:**
    ```sh
    docker compose -f docker-compose.dev.yml up -d
    ```
2.  **Run the application from your IDE:**
    *   Open the project in your favorite IDE (e.g., IntelliJ IDEA).
    *   Run the `Application` class. The `dev` profile is active by default.

The application will be available at `http://localhost:9998`.

## Production

To run the application in a production-like environment, use the following command:

```sh
docker compose -f docker-compose.prod.yml up -d --build
```

This will start the application and a MySQL database container in detached mode.

## Database Migrations

This project uses [Liquibase](https://www.liquibase.org/) to manage database schema changes. The migration scripts are located in the `src/main/resources/db/changelog` directory.

### Generating Migrations

When you make changes to the JPA entities, you can automatically generate a new migration script by running the following command:

```sh
mvn liquibase:diff -Dliquibase.diffChangeLogFile=src/main/resources/db/changelog/changes/<new-version>-<description>.yaml
```

Replace `<new-version>` with the next version number (e.g., `002`) and `<description>` with a brief description of the changes.

## Testing

This project uses [Testcontainers](https://www.testcontainers.org/) to automatically manage a MySQL container for testing. This ensures a consistent and reliable testing environment.

To run all tests, use the following command:

```sh
mvn clean verify
```

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

Please see the [CONTRIBUTING.md](CONTRIBUTING.md) file for details on our code of conduct, and the process for submitting pull requests to us.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

Subhrodip Mohanta - [@SubhrodipMohanta](https://twitter.com/sohbus) - contact@subho.xyz

Project Link: [https://github.com/ohbus/retail-banking](https://github.com/ohbus/retail-banking)