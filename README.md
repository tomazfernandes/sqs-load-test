## Simple Load Test Application for Spring Cloud AWS SQS 
This is a simple application to perform load tests with the new `Spring Cloud AWS SQS` integration.
This is not supposed to be an exhaustive testing suite, only to have some throughput estimation.

It currently supports only `Standard SQS` queues and run results are stored in-memory.
Further improvements include adding support for `FIFO` queues and persisting messages and results to `DynamoDB` tables.

## Results

Results so far with a **single ECS instance** show up to:
* **17K** messages / second with 4 vcpu and 8 GB memory
* **8K** messages / second with 2 vcpu and 4GB memory
* **4K** messages / second with 1 vcpu and 2GB memory

Tests were made with up to **1 million messages** in a single standard SQS queue.

## Test run

The test run consists of:
* Create a standard SQS queue
* Send messages to the queue with a small `String` payload
* Create and start a `SqsMessageListenerContainer`
* Wait for all messages to be consumed
* Wait for all messages to be acknowledged
* Assert results
* Delete the queue

## Using the app

`POST` /sqs-load-test/v1/runs

```json
{
    "id": "29efe1af-396a-48e0-a5a2-af6897233de3",
    "settings" : {
        "totalMessages": 1000,
        "maxConcurrency": 300,
        "timeoutSeconds": 100
    }
}
```

`GET` /sqs-load-test/v1/runs

`GET` /sqs-load-test/v1/runs/{id}

`DELETE` /sqs-load-test/v1/runs/

`DELETE` /sqs-load-test/v1/runs/{id}

## Infrastructure

This application contains a simple `Terraform` infrastructure to be deployed to `AWS ECS`, along with a `Github Actions` CI/CD pipeline.

## Open API / Swagger

Open API documentation that can be accessed at `/sqs-load-test/swagger-ui/index.html`
