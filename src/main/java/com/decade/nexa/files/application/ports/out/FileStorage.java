package com.decade.nexa.files.application.ports.out;

import org.springframework.core.io.Resource;

public interface FileStorage {
      Resource getResource(String key);
}
