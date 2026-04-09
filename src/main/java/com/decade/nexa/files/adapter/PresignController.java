package com.decade.nexa.files.adapter;

import com.decade.nexa.files.application.PresignedUrlService;
import com.decade.nexa.files.domain.FileIntegrityException;
import com.decade.nexa.files.dto.CompleteUploadRequest;
import com.decade.nexa.files.dto.S3PresignedResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/files")
@AllArgsConstructor
public class PresignController {

      @ExceptionHandler(FileIntegrityException.class)
      @ResponseStatus(value = HttpStatus.UNPROCESSABLE_CONTENT)
      public ProblemDetail handleFileIntegrityException(FileIntegrityException ex) {
            log.warn("File integrity violation", ex);
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
            pd.setTitle("File integrity violation");
            return pd;
      }

      private final PresignedUrlService service;

      @PostMapping("/upload")
      public S3PresignedResponse uploadUrl(@RequestParam String filename) {
            String user = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication().getName();
            return service.generateUploadUrl(filename, user);
      }

      @PostMapping(value = "/finish")
      @ResponseStatus(value = HttpStatus.NO_CONTENT)
      public void finishUpload(@RequestBody CompleteUploadRequest request) {
            service.finishUpload(request);
      }
}
