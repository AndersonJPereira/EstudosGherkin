@Login, @Full
Feature: Login

	@SuccessfulLogin
	Scenario: Successful Login
  Given I am on the login page
  When I log in using valid credentials
  And I click login
  Then the login is successful

	@FailedLogin
	Scenario: Failed Login
  Given I am on the login page
  When I log in using invalid credentials
  And I click login
  Then an error message is shown

#  @tag2
#  Scenario Outline: Login Success
#    Given I want to write a step with <name>
#    When I check for the <value> in step
#    Then I verify the <status> in step

#    Examples: 
#      | name  | value | status  |
#      | name1 |     5 | success |
#      | name2 |     7 | Fail    |
