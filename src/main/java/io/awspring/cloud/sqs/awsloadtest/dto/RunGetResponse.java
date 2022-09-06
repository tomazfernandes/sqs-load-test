package io.awspring.cloud.sqs.awsloadtest.dto;

import io.awspring.cloud.sqs.awsloadtest.model.Run;

import java.util.UUID;

/**
 * @author Tomaz Fernandes
 */
public record RunGetResponse(UUID id, String status, SettingsResponse settings,
                             QueueResponse queue, ResultResponse result) {

    public static RunGetResponse of(Run run) {
        return new RunGetResponse(run.id(), run.status().toString(),
                SettingsResponse.of(run.settings()),
                QueueResponse.of(run.queue()),
                ResultResponse.of(run.result()));
    }
}
