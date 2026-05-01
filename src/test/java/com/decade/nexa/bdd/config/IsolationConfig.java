package com.decade.nexa.bdd.config;

import com.decade.nexa.common.DataCleanUp;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class IsolationConfig {

    private final List<DataCleanUp> cleanups;

    @Before
    public void clean() {
        cleanups.forEach(DataCleanUp::clean);
    }

}
