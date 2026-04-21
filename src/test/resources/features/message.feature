# Created by decade at 4/21/26
Feature: Messaging with the application
  As a user i want to ask the chat bot so that i can quickly find related information
  without manually searching documents
  As a user i want to view message history so that i can look up my previous conversations for my needed information

  Scenario: Asking the chat bot successfully
    Given user "teri@gmail.com" login
    When user ask chat bot "what is sql optimization"
    Then the chat bot answer something

  Scenario: View all messages
    Given user "teri@gmail.com" login
    And he has 4 messages half of them are his
    When he open message list
    Then the message list shows all of his 4 message

  Scenario: View first 4 messages
    Given user "teri@gmail.com" login
    And he has 4 messages half of them are his
    When he queries message before the 4 th message
    Then the 3 messages before that message are returned