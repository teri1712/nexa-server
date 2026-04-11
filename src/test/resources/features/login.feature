# Created by decade at 4/2/26
Feature: Login
  As an user, I want to login using oauth2 and stay with my login session so that I can use the app and play with the company documents
  As an admin, I want to login using my username password, so that I can manage the company resources

  Scenario: Admin login Successfully
    Given an admin exist with username "teri1712" and password "vcl123456"
    When logins with username "teri1712" and password "vcl123456"
    Then should be granted access and their profile information

  Scenario: Admin login with wrong password
    Given an admin exist with username "teri1712" and password "vcl123456"
    When logins with username "teri1712" and password "vcl1234567"
    Then the user should be denied access with "Wrong password" message


  Scenario: Admin login with username does not exist
    Given admin "teri1712" does not exist
    When logins with username "teri1713" and password "vcl1234567"
    Then the user should be denied access with "Username not found" message


  Scenario: User login via Google
    Given user with email "tritm06@gmail.com" and name "teri" grant consent to the application
    When user login to the application with that email
    Then should be granted access and their profile information