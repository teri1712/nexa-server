Feature: Uploading
  As a user i want to upload the company documentation so that I can query related information later

  Scenario: Upload a PDF file successfully
    Given user exist with username "teri1712" and password "12345678"
    And user logins with username "teri1712" and password "12345678"
    When user uploading a pdf file at "sql.pdf"
    Then the document is saved
