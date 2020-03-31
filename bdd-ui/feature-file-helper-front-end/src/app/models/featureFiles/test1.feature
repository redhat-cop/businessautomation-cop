@Feature1
Feature: Retrieve Account Balance
This is a feature comment
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

  @Scenario1
  Scenario Outline: Manager should be able to access all Accounts up to Level 2
  This is a Scenario comment
  on 2 lines
    Given I am a manager
    When I login with my credentials
      | Username | Password    |
      | Bill     | password123 |
    And I request balance for <accountnumber> from the accounts API
    Then I should get <balance> as the response

    Examples:
      | accountnumber | balance           |
      | 11111         | 99                |
      | 22222         | 999               |
      | 33333         | 9999              |
      | 44444         | Account Not Found |

  @Scenario1
  Scenario Outline: Supervisor should be able to access all Accounts up to Level 1
    Given I am a supervisor
    When I login with my credentials
      | Username | Password    |
      | Betty    | password123 |
    And I request balance for <accountnumber> from the accounts API
    Then I should get <balance> as the response

    Examples:
      | accountnumber | balance           |
      | 11111         | Not Authorised    |
      | 22222         | 999               |
      | 33333         | 9999              |
      | 44444         | Account Not Found |

  @Scenario3
  Scenario Outline: Normal user should be able to access only Accounts at Level 0
    Given I am a normal user
    When I login with my credentials
      | Username | Password    |
      | Bob      | password123 |
    And I request balance for <accountnumber> from the accounts API
    Then I should get <balance> as the response

    Examples:
      | accountnumber | balance           |
      | 11111         | Not Authorised    |
      | 22222         | Not Authorised    |
      | 33333         | 9999              |
      | 44444         | Account Not Found |