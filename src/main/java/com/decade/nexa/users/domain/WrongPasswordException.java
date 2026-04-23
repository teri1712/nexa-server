package com.decade.nexa.users.domain;

public class WrongPasswordException extends Exception {
      public WrongPasswordException() {
            super("Passwords do not match");
      }
}
