package io.awspring.cloud.sqs.awsloadtest.service;

import io.awspring.cloud.sqs.awsloadtest.RunFailedException;
import io.awspring.cloud.sqs.awsloadtest.model.Run;
import io.awspring.cloud.sqs.awsloadtest.model.Result;
import io.awspring.cloud.sqs.awsloadtest.model.Status;
import io.awspring.cloud.sqs.awsloadtest.model.Queue;
import io.awspring.cloud.sqs.listener.SqsMessageListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Tomaz Fernandes
 */
@Service
public class RunService {

    private static final Logger logger = LoggerFactory.getLogger(RunService.class);

    private final SqsAsyncClient sqsAsyncClient;

    private static final String STANDARD_SQS_QUEUE_NAME_PREFIX = "run-standard-queue-";

    public RunService(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
    }

    public Run createAndStart(Run input) {
        logger.info("Creating queue for run {} with settings {}", input.id(), input.settings());
        // Create standard queue
        String queueName = STANDARD_SQS_QUEUE_NAME_PREFIX + input.id();
        String queueUrl = createQueue(input, queueName);
        try {
            return startRun(input, queueName, queueUrl);
        }
        finally {
            deleteQueue(input.id(), queueName, queueUrl);
        }
    }

    private Run startRun(Run input, String queueName, String queueUrl) {
        UUID runId = input.id();
        Integer totalMessages = input.settings().totalMessages();
        int logEvery = Math.max(totalMessages / 100, 10);
        sendMessages(runId, queueName, queueUrl, totalMessages, logEvery);

        // createContainer
        CountDownLatch messageProcessingLatch = new CountDownLatch(totalMessages);
        SqsMessageListenerContainer<Object> container = createContainer(input, queueName, queueUrl, messageProcessingLatch, logEvery);

        // Start container
        logger.info("Starting container for run {}", runId);
        Instant runStart = Instant.now();
        container.start();

        // Wait for latch
        boolean hasCompleted = waitForLatch(messageProcessingLatch);
        Instant runEnd = Instant.now();

        logger.info("Run completed, stopping container for run {}.", runId);
        container.stop();

        BigDecimal runDurationSeconds = getRunDurationSeconds(runStart, runEnd);
        BigDecimal messagesPerSecond = evaluateResult(input, queueName, totalMessages, hasCompleted, runDurationSeconds);

        return new Run(runId, getRunFinalStatus(hasCompleted), input.settings(),
                new Result(runStart, runEnd, runDurationSeconds.toPlainString(), messagesPerSecond.toPlainString(), null), new Queue(queueName, queueUrl));
    }

    private void deleteQueue(UUID id, String queueName, String queueUrl) {
        logger.info("Deleting queue {} after run {}", queueName, id);
        sqsAsyncClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queueUrl).build()).join();
        logger.info("Queue {} deleted after run {}", queueName, id);
    }

    private BigDecimal evaluateResult(Run input, String queueName, Integer totalMessages, boolean hasCompleted, BigDecimal runDurationSeconds) {
        // Return the result
        if (hasCompleted) {
            BigDecimal messagesPerSecond = new BigDecimal(totalMessages).divide(runDurationSeconds, getMathContext());
            logger.info("Run {} completed successfully in {}s. Messages per second: {}", input.id(), runDurationSeconds, messagesPerSecond);
            return messagesPerSecond;
        }
        logger.warn("Run {} not completed in {}s in queue {}.", input.id(), runDurationSeconds.round(getMathContext()), queueName);
        return BigDecimal.valueOf(0);
    }

    private SqsMessageListenerContainer<Object> createContainer(Run input, String queueName, String queueUrl, CountDownLatch messageProcessingLatch, int logEvery) {
        AtomicInteger receivedMessagesCount = new AtomicInteger();
        AtomicInteger acknowledgedMessages = new AtomicInteger();

        // Create container
        return SqsMessageListenerContainer
                .builder()
                .sqsAsyncClient(this.sqsAsyncClient)
                .id(queueName)
                .configure(options -> options.maxInflightMessagesPerQueue(input.settings().maxConcurrency()))
                .queueNames(queueUrl)
                .asyncMessageListener(msg -> {
                    int received = receivedMessagesCount.incrementAndGet();
                    if (received % logEvery == 0) {
                        logger.info("Received {} messages", received);
                    }
                    messageProcessingLatch.countDown();
                    return CompletableFuture.completedFuture(null);
                })
                .build();
    }

    private void sendMessages(UUID id, String queueName, String queueUrl, Integer totalMessages, int logEvery) {
        Assert.isTrue(totalMessages % 10 == 0, "Total messages must be a factor of ten. Provided: " + totalMessages);
        // Send messages
        // Limit to 1K messages
        logger.info("Sending {} messages  for run {}", totalMessages, id);
        AtomicInteger sentMessagesCount = new AtomicInteger();
        int numberOfBatches = totalMessages / 10;
        IntStream.range(0, numberOfBatches)
                        .forEach(index -> {
                            sqsAsyncClient.sendMessageBatch(createSendMessageBatchRequest(queueName, queueUrl, index).build()).whenComplete((t, v) -> {
                                int sentMessages = sentMessagesCount.addAndGet(10);
                                if (sentMessages / logEvery == 0) {
                                    logger.info("Sent {} messages for run {}", sentMessages, id);
                                }
                            });
                        });
    }

    private String createQueue(Run input, String queueName) {
        String queueUrl = sqsAsyncClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build()).join().queueUrl();
        logger.info("Created queue with name {} url {} for run {}", queueName, queueUrl, input.id());
        return queueUrl;
    }

    private SendMessageBatchRequest.Builder createSendMessageBatchRequest(String queueName, String queueUrl, int index) {
        return SendMessageBatchRequest.builder().queueUrl(queueUrl).entries(createEntries(index, queueName));
    }

    private Status getRunFinalStatus(boolean hasCompleted) {
        return hasCompleted ? Status.COMPLETED : Status.TIMED_OUT;
    }

    private BigDecimal getRunDurationSeconds(Instant startTime, Instant runEnd) {
        return new BigDecimal(Duration.between(startTime, runEnd).toMillis()).divide(new BigDecimal(1000), getMathContext());
    }

    private MathContext getMathContext() {
        return new MathContext(2, RoundingMode.HALF_EVEN);
    }

    private boolean waitForLatch(CountDownLatch runLatch) {
        try {
            return runLatch.await(25, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RunFailedException("Run failed", e);
        }
    }

    private Collection<SendMessageBatchRequestEntry> createEntries(int outerIndex, String queueName) {
        return IntStream.range(0, 10).mapToObj(index -> SendMessageBatchRequestEntry
                        .builder()
                        .messageBody("Test message " + (outerIndex * 10 + index) + " in queue " + queueName)
                        .id(UUID.randomUUID().toString())
                        .build())
                .collect(Collectors.toList());
    }

}
