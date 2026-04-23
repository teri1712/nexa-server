package com.decade.nexa.files.application.ports.out;

import com.decade.nexa.files.apis.FileIntegrityException;
import org.springframework.core.io.Resource;

import java.util.Map;

public interface FileStorage {

      Map<String, String> getFile(String fileKey, String eTag) throws FileIntegrityException;

      Resource getResource(String fileKey);
}
