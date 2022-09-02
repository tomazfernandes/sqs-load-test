package io.awspring.cloud.sqs.awsloadtest.model;

/**
 * The status of the {@link Run} with a timestamp.
 * @author Tomaz Fernandes
 */
public enum Status {

    /**
     * Initial state
     */
    CREATED,

    /**
     * Execution has been validated and will start
     */
    STARTING,

    /**
     * Sending messages to the queue
     */
    SENDING,

    /**
     * Receiving messages from the queue
     */
    RECEIVING,

    /**
     * Execution completed successfully
     */
    COMPLETED,

    /**
     * Execution completed with an error
     */
    COMPLETED_WITH_ERRORS,

    TIMED_OUT;

}
