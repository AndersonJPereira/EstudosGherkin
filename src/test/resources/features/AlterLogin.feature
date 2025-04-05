@AlterLogin @Full
Feature: AlterLogin


  @LoginExamples
  Scenario Outline: Login
    Given I am on the login page
    When I use "<status>" credentials
    Then the login result is "<result>"

    Examples: 
      | status  | result  |
      | valid   | success |
      | invalid | fail    |
