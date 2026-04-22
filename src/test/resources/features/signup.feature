Feature: Sign Up

  As an admin, I want to register other account, so that I can expand my company resources management to other users

  Scenario: Sign up user successfully
    Given admin "teri1712" does not exist
    And an admin exist with username "admin1712" and password "12345678"

    When sign up new account with username "teri1712" and password "12345678" by admin "admin1712" - "12345678"

    And logins with username "teri1712" and password "12345678"
    Then should be granted access and their profile information

  Scenario: Sign up admin successfully
    Given an admin exist with username "teri1712" and password "12345678"
    And logins with username "teri1712" and password "12345678"

    When the admin sign up a new admin account with username "teri17121" and password "12345678"

    Then logins with username "teri17121" and password "12345678"
    And should be granted access and their profile information

  Scenario: Sign up user failed
    Given an admin exist with username "teri1712" and password "12345678"
    And an admin exist with username "admin1712" and password "12345678"

    When sign up new account with username "teri1712" and password "12345678" by admin "admin1712" - "12345678"

    Then fails with error "User already exist"