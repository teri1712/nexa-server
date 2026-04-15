Feature: Uploading
  As a user i want to upload the company documentation so that I can query related information later

  Scenario: Upload a PDF file successfully
    Given an admin exist with username "teri1712" and password "12345678"
    And logins with username "teri1712" and password "12345678"
    When upload a "PDF" file at "sql.pdf" with title "vcl" and description "vcl"
    Then the document is saved
