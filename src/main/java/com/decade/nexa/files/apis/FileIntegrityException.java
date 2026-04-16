package com.decade.nexa.files.apis;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FileIntegrityException extends Exception {
      private String key;
      private String bucket;
      private String expectedEtag;
      private String actualETag;

      @Override
      public String getMessage() {
            return "File " + key + " in bucket " + bucket + " has been corrupted with Etag" + actualETag;
      }
}
