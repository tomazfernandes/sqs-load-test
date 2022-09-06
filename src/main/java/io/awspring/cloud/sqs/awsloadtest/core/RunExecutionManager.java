package io.awspring.cloud.sqs.awsloadtest.core;

import io.awspring.cloud.sqs.awsloadtest.model.Queue;
import io.awspring.cloud.sqs.awsloadtest.model.Run;
import io.awspring.cloud.sqs.awsloadtest.model.Status;
import io.awspring.cloud.sqs.awsloadtest.repository.InMemoryRunRepository;
import io.awspring.cloud.sqs.awsloadtest.service.QueueService;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Tomaz Fernandes
 */
@Component
public class RunExecutionManager {

    private static final String STANDARD_SQS_QUEUE_NAME_PREFIX = "run-standard-queue-";

    private final AsyncTaskExecutor taskExecutor;

    private final Map<UUID, RunExecutionContext> runningExecutions = new ConcurrentHashMap<>();

    private final QueueService queueService;

    private final SqsAsyncClient sqsAsyncClient;

    private final InMemoryRunRepository runRepository;

    public RunExecutionManager(AsyncTaskExecutor taskExecutor, QueueService queueService, SqsAsyncClient sqsAsyncClient, InMemoryRunRepository runRepository) {
        this.taskExecutor = taskExecutor;
        this.queueService = queueService;
        this.sqsAsyncClient = sqsAsyncClient;
        this.runRepository = runRepository;
    }

    public Run startRun(Run run) {
        UUID id = run.id();
        Assert.state(runningExecutions.values().stream().allMatch(context -> context.runExecution().isCompleted()),
                "There's already a run in progress.");
        String queueName = STANDARD_SQS_QUEUE_NAME_PREFIX + id;
        String queueUrl = queueService.createQueue(queueName);
        try {
            Run startedRun = new Run(run.id(), Status.STARTED, run.settings(), new Queue(queueName, queueUrl), run.result());
            RunExecution runExecution = new RunExecution(startedRun, sqsAsyncClient, queueService, runRepository);
            Future<?> executionFuture = taskExecutor.submit(runExecution);
            this.runningExecutions.put(id, new RunExecutionContext(runExecution, executionFuture));
            return startedRun;
        }
        catch (Exception e) {
            queueService.deleteQueue(queueUrl);
            throw new RuntimeException("Error starting run " + run);
        }
    }

    record RunExecutionContext(RunExecution runExecution, Future<?> executionFuture) {
    }
}
