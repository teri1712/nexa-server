package com.decade.nexa.files.application.ports.out;

import com.decade.nexa.files.domain.FileIntegrityException;

public interface StoragePathGenerator {

      Path generateUpload(String username, String filename);

      void validate(String key, String eTag) throws FileIntegrityException;

      record Path(String key, String url) {
      }
}
