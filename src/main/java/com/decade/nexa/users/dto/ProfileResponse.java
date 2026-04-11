package com.decade.nexa.users.dto;

import java.util.Date;
import java.util.UUID;

public record ProfileResponse(
          UUID id,
          String username,
          String name,
          Date dob,
          String role,
          String gender
) {
}
