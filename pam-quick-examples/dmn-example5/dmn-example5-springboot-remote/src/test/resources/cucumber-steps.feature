Feature: As a bank employee I want to use the rule engine to check if a customer can open a new account
  Scenario: An adult customer want open account
    Given A customer that want to open an account
        |name   |surname|dateOfBirth|
        |Donald |Duck   |1870-06-09 |
    When the bank employee check if the customer can open the account
    Then the system 'ALLOW' the agent to open the account

  Scenario: An underage customer want open account
    Given A customer that want to open an account
        |name   |surname|dateOfBirth|
        |Young  |Rossi  |2020-01-20 |
    When the bank employee check if the customer can open the account
    Then the system 'NOT_ALLOW' the agent to open the account