package io.awspring.cloud.sqs.awsloadtest.model;

import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * @author Tomaz Fernandes
 */
public record Run(UUID id, Status status, Settings settings, @Nullable Result result,
                  @Nullable Queue queueAttributes) {

    public Run(UUID id, Integer totalMessages, Integer maxConcurrency) {
        this(id, Status.CREATED, new Settings(totalMessages, maxConcurrency), null, null);
    }

}
