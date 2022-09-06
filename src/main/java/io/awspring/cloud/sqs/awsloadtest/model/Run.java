package io.awspring.cloud.sqs.awsloadtest.model;

import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * @author Tomaz Fernandes
 */
public record Run(UUID id, Status status, Settings settings, Queue queue, @Nullable Result result) {

    public Run(UUID id, Status status, Settings settings) {
        this(id, status, settings, null, null);
    }

    public Run(Run run, Status status) {
        this(run, status,null);
    }

    public Run(Run run, Status status, Result result) {
        this(run.id(), status, run.settings(), run.queue(), result);
    }
}
