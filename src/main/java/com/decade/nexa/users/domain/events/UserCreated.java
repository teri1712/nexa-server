package com.decade.nexa.users.domain.events;

import java.time.Instant;
import java.util.UUID;

public record UserCreated(
          UUID userId,
          String username,
          String name,
          String gender,
          Instant dob
) {
}
