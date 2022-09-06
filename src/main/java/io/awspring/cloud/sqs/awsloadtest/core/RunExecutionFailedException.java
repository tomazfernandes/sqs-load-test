package io.awspring.cloud.sqs.awsloadtest.core;

/**
 * Exception thrown when a run fails.
 *
 * @author Tomaz Fernandes
 */
public class RunExecutionFailedException extends RuntimeException {

    public RunExecutionFailedException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
