package com.decade.nexa.users.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NeedAParentAdminException extends RuntimeException {
      private final String username;

      @Override
      public String getMessage() {
            return "User " + username + " need a parent admin";
      }
}
