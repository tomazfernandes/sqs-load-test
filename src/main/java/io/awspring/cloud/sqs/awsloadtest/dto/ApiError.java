package io.awspring.cloud.sqs.awsloadtest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public record ApiError(HttpStatus status,
                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss") LocalDateTime timestamp, String message, String debugMessage) {

   public ApiError(HttpStatus status, Throwable ex) {
       this(status,  "Unexpected error", ex);
   }

   public ApiError(HttpStatus status, String message, @Nullable Throwable ex) {
       this(status, LocalDateTime.now(), message, ex != null ? ex.getLocalizedMessage() : "");
   }

    public ApiError(HttpStatus status, String message) {
        this(status, message, null);
    }
}