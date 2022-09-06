package io.awspring.cloud.sqs.awsloadtest.dto;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Validated
record SettingsRequest(@Min(10) Integer totalMessages, @Min(10) Integer maxConcurrency, @NotNull Integer timeoutSeconds) {

}