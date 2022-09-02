package io.awspring.cloud.sqs.awsloadtest.dto;

import io.awspring.cloud.sqs.awsloadtest.model.Run;
import io.awspring.cloud.sqs.awsloadtest.model.Queue;
import io.awspring.cloud.sqs.awsloadtest.model.Result;
import io.awspring.cloud.sqs.awsloadtest.model.Settings;
import io.awspring.cloud.sqs.awsloadtest.model.Status;
import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * @author Tomaz Fernandes
 */
public record RunCreateResponse(UUID id, Status status, Settings settings, @Nullable Result result,
                                @Nullable Queue queueAttributes) {

    public static RunCreateResponse of(Run run) {
        return new RunCreateResponse(run.id(), run.status(), run.settings(), run.result(), run.queueAttributes());
    }
}
