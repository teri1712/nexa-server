package com.decade.nexa.users.domain.events;

import java.util.Date;
import java.util.UUID;

public record UserCreated(
          UUID userId,
          String username,
          String name,
          String gender,
          Date dob,
          String avatar
) {
}
