package io.awspring.cloud.sqs.awsloadtest.dto;

import io.awspring.cloud.sqs.awsloadtest.model.Result;
import io.awspring.cloud.sqs.awsloadtest.model.RunError;
import org.springframework.lang.Nullable;

/**
 * @author Tomaz Fernandes
 */
public record ResultResponse(String runStart, String runEnd,
                             String runDurationSeconds, String messagesPerSecond, @Nullable RunError runError) {

    public static ResultResponse of(@Nullable Result result) {
        return result == null
                ? null
                : new ResultResponse(result.runStart().toString(), result.runEnd().toString(), result.runDurationSeconds().toPlainString(), result.messagesPerSecond().toPlainString(),  result.runError());
    }

}
