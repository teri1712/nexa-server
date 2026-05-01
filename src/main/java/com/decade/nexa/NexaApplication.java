package com.decade.nexa;

import org.springframework.boot.SpringApplication;
import org.springframework.modulith.Modulith;

@Modulith(sharedModules = "common")
public class NexaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexaApplication.class, args);
    }

}
