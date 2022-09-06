package io.awspring.cloud.sqs.awsloadtest.service;

import io.awspring.cloud.sqs.awsloadtest.core.RunExecutionManager;
import io.awspring.cloud.sqs.awsloadtest.model.Status;
import io.awspring.cloud.sqs.awsloadtest.repository.RunNotFoundException;
import io.awspring.cloud.sqs.awsloadtest.model.Run;
import io.awspring.cloud.sqs.awsloadtest.repository.RunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Tomaz Fernandes
 */
@Service
public class RunService {

    private static final Logger logger = LoggerFactory.getLogger(RunService.class);

    private final RunExecutionManager runManager;

    private final RunRepository runRepository;

    public RunService(RunExecutionManager runManager, RunRepository runRepository) {
        this.runManager = runManager;
        this.runRepository = runRepository;
    }

    public Run createAndStart(Run input) {
        Run existingRun = runRepository.findById(input.id());
        if (existingRun != null) {
            return new Run(existingRun.id(), Status.STARTED, existingRun.settings(), existingRun.queue(), existingRun.result());
        }
        Run startedRun = runManager.startRun(input);
        return runRepository.save(startedRun);
    }

    public Run findById(UUID runId) {
        Run run = runRepository.findById(runId);
        if (run == null) {
            throw new RunNotFoundException("Run with id " + runId + " not found.");
        }
        return run;
    }

    public Collection<Run> findAll() {
        return runRepository.findAll();
    }

    public Collection<Run> deleteAll() {
        return runRepository.deleteAll();
    }

    public Run delete(UUID id) {
        return runRepository.delete(id);
    }

}
