package com.decade.nexa.files.apis;

import org.springframework.core.io.Resource;

public record FileResource(String filename, String fileType, Resource resource, String url) {
}
