package io.awspring.cloud.sqs.awsloadtest.dto;

import io.awspring.cloud.sqs.awsloadtest.model.Settings;

record SettingsResponse(Integer totalMessages, Integer maxConcurrency) {
    public static SettingsResponse of(Settings settings) {
        return new SettingsResponse(settings.totalMessages(), settings.maxConcurrency());
    }
}
