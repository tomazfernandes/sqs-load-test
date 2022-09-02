package io.awspring.cloud.sqs.awsloadtest.dto;

import io.awspring.cloud.sqs.awsloadtest.model.Run;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * @author Tomaz Fernandes
 */
@Validated
public record RunCreateRequest(@NotNull UUID id, @Min(1) @Max(1000) Integer totalMessages, @Min(1) @Max(100) Integer maxConcurrency) {

    public Run toEntity() {
        return new Run(id, totalMessages, maxConcurrency);
    }

}
