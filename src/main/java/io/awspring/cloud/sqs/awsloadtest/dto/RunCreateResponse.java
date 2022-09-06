package io.awspring.cloud.sqs.awsloadtest.dto;

import io.awspring.cloud.sqs.awsloadtest.model.Run;

import java.net.URI;
import java.util.UUID;

/**
 * DTO for the create run response.
 * @author Tomaz Fernandes
 */
public record RunCreateResponse(UUID id, String status, SettingsResponse settings,
                                 QueueResponse queueAttributes, URI runLocation) {

    public static RunCreateResponse of(Run run, URI uri) {
        return new RunCreateResponse(run.id(), run.status().toString(),
                SettingsResponse.of(run.settings()),
                QueueResponse.of(run.queue()),
                uri);
    }
}
