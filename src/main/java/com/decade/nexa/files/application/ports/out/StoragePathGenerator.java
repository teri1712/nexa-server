package com.decade.nexa.files.application.ports.out;

public interface StoragePathGenerator {

    Path generateUpload(String username, String filename);

    Path generateDownload(String username, String fileKey);

    record Path(String key, String url) {
    }
}
