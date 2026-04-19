package com.decade.nexa.files.adapter;

import com.decade.nexa.files.application.PresignedUrlService;
import com.decade.nexa.files.dto.PresignedDownloadResponse;
import com.decade.nexa.files.dto.PresignedUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/files")
@AllArgsConstructor
public class PresignController {

    private final PresignedUrlService service;

    @PostMapping("/upload")
    @Operation(summary = "Generate presigned url for uploading file")
    @SecurityRequirement(name = "bearerAuth")
    public PresignedUploadResponse uploadUrl(@RequestParam String filename) {
        String user = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication().getName();
        return service.generateUploadUrl(filename, user);
    }

    @PostMapping("/download")
    @Operation(summary = "Generate presigned url for downloading file")
    @SecurityRequirement(name = "bearerAuth")
    public PresignedDownloadResponse downloadUrl(@RequestParam String filekey) {
        String user = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication().getName();
        return service.generateDownloadUrl(filekey, user);
    }
}
