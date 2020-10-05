# Contributing to Griffin Bank of People

We love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer


## Steps to contribute

* Comment on the issue you want to work on. Make sure it's not assigned to someone else.

### Making a Pull Request

> - Make sure you have been assigned the issue to which you are making a PR.
> - If you make PR before being assigned, It will be labeled `invalid` and closed without merging.

* Fork the repo and clone it on your machine.
    ```
    git clone https://github.com/<your-github-id>/retail-banking.git
    cd retail-banking
    ```
* Add a upstream link to main branch in your cloned repo
    ```
    git remote add upstream https://github.com/ohbus/retail-banking.git
    ```
* Keep your cloned repo upto date by pulling from upstream (this will also avoid any merge conflicts while committing new changes)
    ```
    git pull upstream master
    ```
* Create your feature branch
    ```
    git checkout -b <feature-name>
    ```
* Commit all the changes
    ```
    git commit -am "Meaningful commit message"
    ```
* Push the changes for review
    ```
    git push origin <branch-name>
    ```
* Create a Pull Request from our repo on GitHub.

## Setting up the development Enviornment using [Eclipse (Java EE)](https://www.eclipse.org/downloads/packages/release/2020-09/r/eclipse-ide-enterprise-java-developers) or [STS](https://spring.io/tools)

> - You can always use your favourite IDE like [IntelliJ IDEA](https://www.jetbrains.com/idea/) or [VS Code](https://code.visualstudio.com/)
> - Please check that the [JAVA_HOME](https://docs.oracle.com/cd/E19182-01/821-0917/inst_jdk_javahome_t/index.html) enviornment variable is setup using [JDK 11](https://adoptopenjdk.net/) to ensure optimal results.
- Import the project into your Workspace as an Existing Maven Project.
- Go to the file called [application.properties](https://github.com/ohbus/retail-banking/blob/master/src/main/resources/application.properties) under `src/main/resources`
- Here you will find four enviornment variable namely
    - **`GCP_MYSQL_DB_HOST`** and **`GCP_MYSQL_DB_PORT`** at [Line 6](https://github.com/ohbus/retail-banking/blob/ff51f236a05dbd5e8cdfdbb406ef35550987a5e1/src/main/resources/application.properties#L6)
    - **`GCP_MYSQL_DB_UNAME`** and **`GCP_MYSQL_DB_PASSWD`** at [Line 9](https://github.com/ohbus/retail-banking/blob/ff51f236a05dbd5e8cdfdbb406ef35550987a5e1/src/main/resources/application.properties#L9) and [10](https://github.com/ohbus/retail-banking/blob/ff51f236a05dbd5e8cdfdbb406ef35550987a5e1/src/main/resources/application.properties#L10) respectively.
    - You can either set this enviornment variables under your Maven Build and SpringBoot run OR
    - You can also **remove** these and enter the following in place of the enviornmet varaibles
        - `GCP_MYSQL_DB_HOST` will be your MySQL or MariaDB **host**. Like `localhost` or `127.0.0.1` or `0.0.0.0`
        - `GCP_MYSQL_DB_PORT` will be the **port** at which your database server resides. Like `3306`
        - `GCP_MYSQL_DB_UNAME` will be the **username** for your database. Like `root`
        - `GCP_MYSQL_DB_PASSWD` will be the **password** for accessing the database. Like `root123`
- After this right click on the project and select Run As **Maven Build**
- Under **goals** write **`clean verify`** and Run the Build. _This produces an executable JAR under the `target` folder_
- After a Successful Build. Run the application by Right Clicking on the project and clicking on Run as *Spring Boot App*
- You can access the application under **port 8080**
- If you want to change the port on which the application is running. Please make the changes in [application.properties](https://github.com/ohbus/retail-banking/blob/master/src/main/resources/application.properties) at Line [41](https://github.com/ohbus/retail-banking/blob/ff51f236a05dbd5e8cdfdbb406ef35550987a5e1/src/main/resources/application.properties#L41) and uncomment the line and enter your desired port.
- If you are still facing any difficulties Please raise an [Issue](https://github.com/ohbus/retail-banking/issues/new/choose) or reach out to [me](https://subho.xyz/site/en/contact.html).
- If you feel the documentation needs an update. Please send a Pull Request to update the same.


### Additional Notes

* Code should be properly commented to ensure it's readability.
* If you've added code that should be tested, add tests as comments.
* Make sure your code properly formatted.
* Issue that pull request!
* You code should pass the automated CI pipleine Tests before making into the codebase.
* Use Maven Wrapper for Java Code code `mvnw clean verify` for Windows and `./mvnw clean verify` for UNIX based systems.

## Issue suggestions/Bug reporting

When you are creating an issue, make sure it's not already present. Furthermore, provide a proper description of the changes. If you are suggesting any code improvements, provide through details about the improvements.

**Great Issue suggestions** tend to have:

- A quick summary of the changes.
- In case of any bug provide steps to reproduce
  - Be specific!
  - Give sample code if you can.
  - What you expected would happen
  - What actually happens
  - Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)


## License

By contributing, you agree that your contributions will be licensed under its  [GPL-3.0 License](https://github.com/ohbus/retail-banking/blob/master/LICENSE).
