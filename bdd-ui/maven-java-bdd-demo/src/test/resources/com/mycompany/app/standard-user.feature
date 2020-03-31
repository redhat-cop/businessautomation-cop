Feature: Standard User access to Accounts
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

  Scenario Outline: Standard user should be able to access only Accounts at Level 0
    Given I am a standard user
    When I login with my credentials
      | Username | Password    |
      | Bob      | password123 |
    And I request balance for <accountnumber> from the accounts API
    Then I should get <balance> as the balance request response

    Examples:
      | accountnumber | balance           |
      | 11111         | Not Authorised    |
      | 22222         | Not Authorised    |
      | 33333         | 9999              |
      | 44444         | Account Not Found |