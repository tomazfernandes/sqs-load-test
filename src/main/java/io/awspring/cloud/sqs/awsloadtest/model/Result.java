package io.awspring.cloud.sqs.awsloadtest.model;

import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Tomaz Fernandes
 */
public record Result(Instant runStart, Instant runEnd,
                     BigDecimal runDurationSeconds, BigDecimal messagesPerSecond, @Nullable RunError runError) {

}
