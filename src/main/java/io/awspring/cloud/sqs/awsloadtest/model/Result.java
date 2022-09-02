package io.awspring.cloud.sqs.awsloadtest.model;

import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Tomaz Fernandes
 */
public record Result(@Nullable Instant runStart, @Nullable Instant runEnd,
                     String runDurationSeconds, String messagesPerSecond, @Nullable RunError runError) {
}
