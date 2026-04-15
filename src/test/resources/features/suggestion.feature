Feature: AI suggestion
  As a user i want AI to suggest me a brief answer for my query, so that I can find needed information fastly

  Scenario: User ask something
    Given file pdf "sql.pdf" is alr uploaded
    And user "teri1712@gmail.com" login
    When user query "What are SQL optimization techniques"
    Then AI suggest some information
