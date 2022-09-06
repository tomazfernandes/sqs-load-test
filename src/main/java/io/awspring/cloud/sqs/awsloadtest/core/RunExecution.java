package io.awspring.cloud.sqs.awsloadtest.core;

import io.awspring.cloud.sqs.CompletableFutures;
import io.awspring.cloud.sqs.awsloadtest.model.Queue;
import io.awspring.cloud.sqs.awsloadtest.model.Result;
import io.awspring.cloud.sqs.awsloadtest.model.Run;
import io.awspring.cloud.sqs.awsloadtest.model.RunError;
import io.awspring.cloud.sqs.awsloadtest.model.Status;
import io.awspring.cloud.sqs.awsloadtest.repository.RunRepository;
import io.awspring.cloud.sqs.awsloadtest.service.QueueService;
import io.awspring.cloud.sqs.listener.SqsMessageListenerContainer;
import io.awspring.cloud.sqs.listener.acknowledgement.AsyncAcknowledgementResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;

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
public class RunExecution implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RunExecution.class);

    private final Run run;

    private final SqsAsyncClient sqsAsyncClient;

    private final QueueService queueService;

    private final RunRepository runRepository;

    private boolean completed;

    public RunExecution(Run run, SqsAsyncClient sqsAsyncClient, QueueService queueService, RunRepository runRepository) {
        this.run = run;
        this.queueService = queueService;
        this.sqsAsyncClient = sqsAsyncClient;
        this.runRepository = runRepository;
    }

    public Run getRun() {
        return run;
    }

    public void run() {
        try {
            executeRun();
        }
        catch (Exception e) {
            logger.error("Error running test", e);
            runRepository.save(new Run(run, Status.COMPLETED_WITH_ERRORS,
                    new Result(Instant.now(), Instant.now(), BigDecimal.ZERO, BigDecimal.ZERO,
                            new RunError(e.getMessage(), e.getClass().getSimpleName()))));
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    private void executeRun() {
        UUID runId = run.id();
        logger.info("Executing run {}", runId);
        String queueName = run.queue().queueName();
        String queueUrl = run.queue().queueUrl();

        // Send messages
        runRepository.save(new Run(run, Status.SENDING));
        Integer totalMessages = run.settings().totalMessages();
        int logEvery = Math.max(totalMessages / 100, 10);
        sendMessages(runId, queueName, queueUrl, totalMessages, logEvery);

        // createContainer
        CountDownLatch messageProcessingLatch = new CountDownLatch(totalMessages);
        CountDownLatch acknowledgementLatch = new CountDownLatch(totalMessages);
        SqsMessageListenerContainer<Object> container = createContainer(run, messageProcessingLatch, acknowledgementLatch, logEvery);

        // Start container
        logger.info("Starting container for run {}", runId);
        Instant runStart = Instant.now();
        container.start();

        // Wait for latch
        runRepository.save(new Run(run, Status.RECEIVING));
        boolean hasReceivedAllMessages = waitForLatch(messageProcessingLatch);
        logger.info("Received all messages, waiting for acks.");
        boolean hasAckedAllMessages = waitForLatch(acknowledgementLatch);

        boolean hasCompleted = hasReceivedAllMessages && hasAckedAllMessages;

        Instant runEnd = Instant.now();

        logger.info("Run completed, stopping container for run {}.", runId);
        container.stop();

        BigDecimal runDurationSeconds = getRunDurationSeconds(runStart, runEnd);
        BigDecimal messagesPerSecond = evaluateResult(run, queueName, totalMessages, hasCompleted, runDurationSeconds);

        Run completedRun = new Run(runId, getRunFinalStatus(hasReceivedAllMessages), this.run.settings(),
                new Queue(queueName, queueUrl), new Result(runStart, runEnd, runDurationSeconds, messagesPerSecond, null));

        runRepository.save(completedRun);
        queueService.deleteQueue(run.queue().queueUrl());

        completed = true;
    }

    private SqsMessageListenerContainer<Object> createContainer(Run input, CountDownLatch messageProcessingLatch, CountDownLatch acknowledgementLatch, int logEvery) {

        AtomicInteger receivedMessagesCount = new AtomicInteger();

        AtomicInteger acknowledgedMessagesCount = new AtomicInteger();

        // Create container
        return SqsMessageListenerContainer
                .builder()
                .sqsAsyncClient(this.sqsAsyncClient)
                .id(input.queue().queueName())
                .configure(options -> options.maxInflightMessagesPerQueue(input.settings().maxConcurrency()))
                .queueNames(input.queue().queueUrl())
                .asyncMessageListener(msg -> {
                    int received = receivedMessagesCount.incrementAndGet();
                    if (received % logEvery == 0) {
                        logger.info("Received {} messages", received);
                    }
                    messageProcessingLatch.countDown();
                    return CompletableFuture.completedFuture(null);
                })
                .acknowledgementResultCallback(new AsyncAcknowledgementResultCallback<>() {
                    @Override
                    public CompletableFuture<Void> onSuccess(Collection<Message<Object>> messages) {
                        messages.forEach(msg -> {
                            int acked = acknowledgedMessagesCount.incrementAndGet();
                            if (acked % logEvery == 0) {
                                logger.info("Acknowledged {} messages", acked);
                            }
                            acknowledgementLatch.countDown();
                        });
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .build();
    }

    private void sendMessages(UUID id, String queueName, String queueUrl, Integer totalMessages, int logEvery) {
        Assert.isTrue(totalMessages % 10 == 0, "Total messages must be a factor of ten. Provided: " + totalMessages);
        // Send messages
        logger.info("Sending {} messages  for run {}", totalMessages, id);
        AtomicInteger sentMessagesCount = new AtomicInteger();
        int numberOfBatches = totalMessages / 10;
        CompletableFuture.allOf(IntStream.range(0, numberOfBatches)
                .mapToObj(index -> CompletableFutures.exceptionallyCompose(doSendMessages(id, queueName, queueUrl, logEvery, sentMessagesCount, index), t -> {
                            logger.error("Error sending messages, retrying", t);
                            return doSendMessages(id, queueName, queueUrl, logEvery, sentMessagesCount, index);
                        })
                )
                .toArray(CompletableFuture[]::new)).join();
    }

    private CompletableFuture<SendMessageBatchResponse> doSendMessages(UUID id, String queueName, String queueUrl, int logEvery, AtomicInteger sentMessagesCount, int index) {
        int sentMessages = sentMessagesCount.addAndGet(10);
        throttle(sentMessages);
        if (sentMessages % logEvery == 0) {
            logger.info("Sent {} messages for run {}", sentMessages, id);
        }
        return sqsAsyncClient.sendMessageBatch(createSendMessageBatchRequest(queueName, queueUrl, index).build());
    }

    private void throttle(int sentMessages) {
        if (sentMessages % 100 == 0) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while sleeping", e);
            }
        }
    }

    private SendMessageBatchRequest.Builder createSendMessageBatchRequest(String queueName, String queueUrl, int index) {
        return SendMessageBatchRequest.builder().queueUrl(queueUrl).entries(createEntries(index, queueName));
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

    private Status getRunFinalStatus(boolean hasCompleted) {
        return hasCompleted ? Status.COMPLETED : Status.TIMED_OUT;
    }

    private BigDecimal getRunDurationSeconds(Instant startTime, Instant runEnd) {
        return new BigDecimal(Duration.between(startTime, runEnd).toMillis()).divide(new BigDecimal(1000), getMathContext());
    }

    private MathContext getMathContext() {
        return new MathContext(20, RoundingMode.HALF_EVEN);
    }

    private boolean waitForLatch(CountDownLatch runLatch) {
        try {
            return runLatch.await(run.settings().timeoutSeconds(), TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RunExecutionFailedException("Run failed", e);
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
