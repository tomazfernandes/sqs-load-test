package io.awspring.cloud.sqs.awsloadtest.model;

/**
 * @author Tomaz Fernandes
*/
public record Settings(Integer totalMessages, Integer maxConcurrency, Integer timeoutSeconds) {
}
