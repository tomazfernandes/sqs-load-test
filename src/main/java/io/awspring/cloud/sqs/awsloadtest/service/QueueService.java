package io.awspring.cloud.sqs.awsloadtest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;

/**
 * @author Tomaz Fernandes
 */
@Component
public class QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);

    private final SqsAsyncClient sqsAsyncClient;

    public QueueService(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
    }

    public String createQueue(String queueName) {
        Assert.isTrue(StringUtils.hasText(queueName), "queueName must have text");
        logger.info("Creating queue {}}", queueName);
        String queueUrl = sqsAsyncClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build()).join().queueUrl();
        logger.info("Created queue with name {} url {}", queueName, queueUrl);
        return queueUrl;
    }

    public void deleteQueue(String queueUrl) {
        Assert.isTrue(StringUtils.hasText(queueUrl), "queueUrl must have text");
        logger.info("Deleting queue {}", queueUrl);
        sqsAsyncClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queueUrl).build()).join();
        logger.info("Queue {} deleted", queueUrl);
    }

}
