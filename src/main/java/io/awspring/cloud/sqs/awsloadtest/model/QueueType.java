package io.awspring.cloud.sqs.awsloadtest.model;

/**
 * The type of the queue to perform tests against.
 * @author Tomaz Fernandes
 */
public enum QueueType {

    /**
     * Standard SQS Queue
     */
    STANDARD,

    /**
     * FIFO SQS Queue
     */
    FIFO

}
