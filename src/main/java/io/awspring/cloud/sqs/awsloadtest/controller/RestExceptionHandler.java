package io.awspring.cloud.sqs.awsloadtest.controller;

import io.awspring.cloud.sqs.awsloadtest.repository.RunNotFoundException;
import io.awspring.cloud.sqs.awsloadtest.dto.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Tomaz Fernandes
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RunNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(RunNotFoundException ex) {
        ApiError apiError = new ApiError(NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), NOT_FOUND);
    }
}
