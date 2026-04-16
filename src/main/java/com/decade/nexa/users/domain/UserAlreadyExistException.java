package com.decade.nexa.users.domain;

import lombok.Getter;

@Getter
public class UserAlreadyExistException extends Exception {
      private final String username;

      public UserAlreadyExistException(String username, Throwable cause) {
            super(cause);
            this.username = username;
      }


      @Override
      public String getMessage() {
            return "User " + username + " already exist";
      }
}
