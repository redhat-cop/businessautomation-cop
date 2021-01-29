Feature: Examples on how use PAM 7.x bdd integration framework

  Scenario: A process with human task
    Given an instance of 'project-to-test-with-bdd.process-with-humantask' is started
    When the user 'BankEmployee' claims the task
      |task.filter.name|Fill in customer data |
    And the user 'BankEmployee' starts the task
      |task.filter.name|Fill in customer data |
    And the user 'BankEmployee' completes the task
      |task.filter.name|Fill in customer data |
      |name            |Claudio               |
      |surname         |Luppi                 |
    Then the process instance status is 'COMPLETED'
    And the process variables values are
      |name   |Claudio |
      |surname|Luppi   |