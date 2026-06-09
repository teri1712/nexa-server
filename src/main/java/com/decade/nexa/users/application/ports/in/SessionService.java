package com.decade.nexa.users.application.ports.in;

import com.decade.nexa.users.dto.AccountResponse;

public interface SessionService {

    AccountResponse login(String username);

}
