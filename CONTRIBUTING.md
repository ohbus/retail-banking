# Contributing to Retail Banking

First off, thank you for considering contributing to Retail Banking! It's people like you that make the open-source community such a great place. We welcome any and all contributions, from bug reports to new features.

## Code of Conduct

This project and everyone participating in it is governed by the [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [contact@subho.xyz](mailto:contact@subho.xyz).

## How to Contribute

There are many ways to contribute to Retail Banking, and we appreciate all of them.

### Reporting Bugs

If you find a bug, please open an issue on our [GitHub issue tracker](https://github.com/ohbus/retail-banking/issues). When you are creating a bug report, please include as many details as possible. Fill out the required template, the information it asks for helps us resolve issues faster.

### Suggesting Enhancements

If you have an idea for a new feature or an enhancement to an existing one, please open an issue on our [GitHub issue tracker](https://github.com/ohbus/retail-banking/issues). Please provide a clear and detailed explanation of the feature you are proposing, including any potential use cases.

### Pull Requests

If you have a bug fix or a new feature that you would like to contribute, please open a pull request. When you are creating a pull request, please make sure that:

1.  You have read and understood the [Code of Conduct](CODE_OF_CONDUCT.md).
2.  Your code adheres to the [Style Guide](#style-guide).
3.  You have added tests for any new code.
4.  You have run all the tests and they all pass.
5.  You have updated the documentation to reflect any changes.

## Database Migrations

This project uses [Liquibase](https://www.liquibase.org/) to manage database schema changes. The migration scripts are located in the `src/main/resources/db/changelog` directory.

When you make changes to the JPA entities, you must generate a new migration script. You can do this by running the following command:

```sh
mvn liquibase:diff -Dliquibase.diffChangeLogFile=src/main/resources/db/changelog/changes/<new-version>-<description>.yaml
```

Replace `<new-version>` with the next version number (e.g., `002`) and `<description>` with a brief description of the changes.

## Running Tests

This project uses [Testcontainers](https://www.testcontainers.org/) to automatically manage a MySQL container for testing. This ensures a consistent and reliable testing environment.

To run all tests, use the following command:

```sh
mvn clean verify
```

## Style Guide

We use the standard Java coding conventions. Please make sure that your code adheres to these conventions before submitting a pull request.

*   **Indentation:** Use 4 spaces for indentation.
*   **Line Endings:** Use Unix-style line endings (`\n`).
*   **Comments:** Use Javadoc comments for all public methods and classes.
*   **Naming Conventions:** Use camelCase for all variables and methods, and PascalCase for all classes.

## License

By contributing to Retail Banking, you agree that your contributions will be licensed under the [MIT License](LICENSE).