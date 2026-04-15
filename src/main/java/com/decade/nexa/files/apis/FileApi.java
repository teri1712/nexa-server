package com.decade.nexa.files.apis;

import org.springframework.core.io.Resource;

public interface FileApi {
      FileMetadata getFile(String fileKey, String eTag);

      Resource getResource(String fileKey);
}
