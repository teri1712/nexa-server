package com.decade.nexa.users.domain;

public class ExpiredTokenException extends InvalidTokenException {
      public ExpiredTokenException(Throwable cause) {
            super(cause);
      }
}
