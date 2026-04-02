# Created by decade at 4/2/26
Feature: Login
  # Enter feature description here

  Scenario: Login Successfully
    Given user exist with username "teri" and password "vcl123456"
    When user logins with username "teri" and password "vcl123456"
    Then the user should be granted access and their profile information

  Scenario: Login with wrong password
    Given user exist with username "teri" and password "vcl123456"
    When user logins with username "teri" and password "vcl1234567"
    Then the user should be denied access with "Wrong Password" message


  Scenario: Login with username does not exist
    Given username "teri1" does not exist
    When user logins with username "teri" and password "vcl1234567"
    Then the user should be denied to access with "Username not exists" message