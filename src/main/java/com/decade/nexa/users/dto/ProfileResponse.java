package com.decade.nexa.users.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    String username,
    String name,
    LocalDate dob,
    String role,
    String gender
) {
}
