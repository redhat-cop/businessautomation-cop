Feature: Example

  Scenario: Basic Test
    Given the following process files:
      | com/pamtests/examplecucumberproject/ExampleProcessOne.bpmn |
      And the process definition ID "ExampleCucumberProject.ExampleProcessOne"
      And a process parameter "message" with value "hello world"
    When the process is started
    Then the following nodes were triggered:
      | Print |
      And the process completed
      And the process is not active

  Scenario: Basic Human Task Test (Claim)
    Given the following process files:
      | com/pamtests/examplecucumberproject/ExampleHumanTaskProcess.bpmn |
      And the process definition ID "ExampleCucumberProject.ExampleHumanTaskProcess"
    When the process is started
    Then the current node is "HumanTask"
      And user "bob" can claim the human task "HumanTask"
      But user "alice" cannot claim the human task "HumanTask"

  Scenario: Basic Human Task Test (Complete - true)
    Given the following process files:
      | com/pamtests/examplecucumberproject/ExampleHumanTaskProcess.bpmn |
      And the process definition ID "ExampleCucumberProject.ExampleHumanTaskProcess"
    When the process is started
    When user "bob" completes the human task "HumanTask" with the following boolean data
      | condition | TRUE |
    Then the following nodes were triggered:
      | HumanTask |
      | ConditionTrueTask |
      And the process completed

  Scenario: Basic Human Task Test (Complete - false)
    Given the following process files:
      | com/pamtests/examplecucumberproject/ExampleHumanTaskProcess.bpmn |
      And the process definition ID "ExampleCucumberProject.ExampleHumanTaskProcess"
    When the process is started
    When user "bob" completes the human task "HumanTask" with the following boolean data
      | condition | FALSE |
    Then the following nodes were triggered:
      | HumanTask |
      | ConditionFalseTask |
      And the process completed
      
      
      
      