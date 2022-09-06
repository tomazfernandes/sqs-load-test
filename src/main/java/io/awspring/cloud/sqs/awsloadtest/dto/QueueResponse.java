package io.awspring.cloud.sqs.awsloadtest.dto;

import io.awspring.cloud.sqs.awsloadtest.model.Queue;

record QueueResponse(String queueName, String queueUrl){

    public static QueueResponse of(Queue queue) {
        return new QueueResponse(queue.queueName(), queue.queueUrl());
    }

}