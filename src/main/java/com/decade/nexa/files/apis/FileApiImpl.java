package com.decade.nexa.files.apis;

import com.decade.nexa.files.application.ports.out.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileApiImpl implements FileApi {

      private final FileStorage fileStorage;

      @Override
      public FileMetadata getFile(String fileKey, String eTag) throws FileIntegrityException {
            Map<String, String> metadata = fileStorage.getFile(fileKey, eTag);
            return new FileMetadata(metadata.get("filename"), metadata.get("filetype"));
      }

      @Override
      public Resource getResource(String fileKey) {
            return fileStorage.getResource(fileKey);
      }
}
