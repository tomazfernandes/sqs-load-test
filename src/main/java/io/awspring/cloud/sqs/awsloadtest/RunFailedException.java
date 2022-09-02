package io.awspring.cloud.sqs.awsloadtest;

/**
 * Exception thrown when a run fails.
 *
 * @author Tomaz Fernandes
 */
public class RunFailedException extends RuntimeException {

    public RunFailedException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
