Feature: Searching documents
  As a user i want to search documents by keywords so that I can find my needed documents

  Scenario: Search with a simple query
    Given file pdf "sql.pdf" is alr uploaded
    And user "teri@gmail.com" login
    When user search with keyword "sql" and content type "PDF"
    Then the document "sql.pdf" must be return and that file type must be "PDF"

  Scenario: Search with date filter
    Given file pdf "sql.pdf" is alr uploaded
    And user "teri@gmail.com" login

    When user search with keyword "sql" and date is yesterday
    Then no documents is return

  Scenario: Search with a not match content type
    Given file pdf "sql.pdf" is alr uploaded
    And user "teri@gmail.com" login
    When user search with keyword "sql" and content type "DOCX"
    Then no documents is return
