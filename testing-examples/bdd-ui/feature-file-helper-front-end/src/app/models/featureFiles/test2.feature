Feature: Is it Friday yet?
  Everybody wants to know when it's Friday

  Scenario: Sunday isn't Friday
    Given today is Sunday
    When I ask whether it's Friday yet
    Then I should be told "Nope"

  Scenario Outline: Friday is Friday
    Given today is <day1>
    When I ask whether it's <day2> yet
    Then I should be told "TGIF"

    Examples:

      | day1 | day2 |
      | mon  | tue  |
      | wed  | thu  |
      | wer  | dfg  |


