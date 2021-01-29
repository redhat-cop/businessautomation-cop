Feature: Examples on how use PAM 7.x bdd integration framework

  Scenario: A process with custom work item handler
    Given an instance of 'project-to-test-with-bdd.process-with-custom-wih' is started
    When the node 'CustomWorkItemHandler' has been triggered
    Then the process instance status is 'COMPLETED'
