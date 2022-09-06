package io.awspring.cloud.sqs.awsloadtest.repository;

/**
 * @author Tomaz Fernandes
 */
public class RunNotFoundException extends RuntimeException{

    public RunNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
