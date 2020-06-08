# Simple BDD Demo App

## Summary

Simple Maven Java Application to demo and test BDD using the Cucumber Maven plugin.

Feature Files are defined at `src/test/resources/com/mycompany/app`

Steps Definitions are defined at `src/test/java/com/mycompany/app/StepDefinitions.java`

Application based on:

* Account Objects, which have an id, balance and authoritisation level: `src/main/java/com/mycompany/app/models/Account.java`

* User Objects, which have a username, password and usertype: `src/main/java/com/mycompany/app/models/User.java`

* Valid User types: user, supervisor and manager.  `src/main/java/com/mycompany/app/models/AuthLevel.java`

The BDD tests / feature files include scenarios to:

* check for valid and invalid login

* check that users can access accounts bases on their usertype and the auth level of the account

  * User type `user` can access balance on accounts at level 0

  * User type `supervisor` can access balance on accounts at level 1 or lower

  * User type `manager` can access balance on accounts at level 2 or lower


## Cucumber dependency

io.cucmber maven dependency added to POM to facilitate BDD testing usung feature files in `src/test/resources/com/mycompany/app`

[Maven Cucmmber](https://mvnrepository.com/artifact/io.cucumber)

[Cucumber.io](https://cucumber.io/)


## Run BDD Tests

```bash
mvn test
```

## Run Application

```bash
mvn clean package -DskipTests
java -jar target/my-app-1.0-SNAPSHOT.jar
```


## More Info / Tutorial

[Tutorial](https://cucumber.io/docs/guides/10-minute-tutorial/)



