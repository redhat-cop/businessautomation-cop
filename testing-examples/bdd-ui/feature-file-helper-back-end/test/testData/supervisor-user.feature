Feature: Supervisor User access to Accounts
  Background:
    Given The following users are valid
      | Username | Password    | UserType   |
      | Bill     | password123 | manager    |
      | Betty    | password123 | supervisor |
      | Bob      | password123 | user       |
    And these accounts exist
      | AccountNum | AccountLevel | Balance |
      | 11111      | 2            | 99      |
      | 22222      | 1            | 999     |
      | 33333      | 0            | 9999    |

  Scenario: Supervisor should be able to to login with correct credentials
    Given I am a supervisor
    When I login with with Username "Betty" and Password "password123"
    Then I should get "user authorised" as the login response

  Scenario: Supervisor should not be able to to login with incorrect credentials
    Given I am a supervisor
    When I login with with Username "Betty" and Password "password456"
    Then I should get "invalid credentials" as the login response

  Scenario Outline: Supervisor should be able to access all Accounts up to Level 1
    Given I am a supervisor
    When I login with my credentials
      | Username | Password    |
      | Betty    | password123 |
    And I request balance for <accountnumber> from the accounts API
    Then I should get <balance> as the balance request response

    Examples:
      | accountnumber | balance           |
      | 11111         | Not Authorised    |
      | 22222         | 999               |
      | 33333         | 9999              |
      | 44444         | Account Not Found |
