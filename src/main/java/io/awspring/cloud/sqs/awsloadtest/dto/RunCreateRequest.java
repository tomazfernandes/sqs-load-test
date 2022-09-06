package io.awspring.cloud.sqs.awsloadtest.dto;

import io.awspring.cloud.sqs.awsloadtest.model.Run;
import io.awspring.cloud.sqs.awsloadtest.model.Settings;
import io.awspring.cloud.sqs.awsloadtest.model.Status;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * @author Tomaz Fernandes
 */
@Validated
public record RunCreateRequest(@NotNull UUID id, @Valid @NotNull SettingsRequest settings) {

    public Run toEntity() {
        return new Run(id, Status.CREATED, new Settings(settings().totalMessages(), settings().maxConcurrency(), settings.timeoutSeconds()));
    }

}
