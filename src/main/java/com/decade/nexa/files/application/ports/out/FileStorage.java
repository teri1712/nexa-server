package com.decade.nexa.files.application.ports.out;

import org.springframework.core.io.Resource;

import java.util.Map;

public interface FileStorage {

      Map<String, String> getFile(String fileKey, String eTag);

      Resource getResource(String fileKey);
}
