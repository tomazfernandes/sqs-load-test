package io.awspring.cloud.sqs.awsloadtest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * @author Tomaz Fernandes
 */
@Configuration
public class LoadTestConfiguration {

    @Bean
    SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder().httpClient(NettyNioAsyncHttpClient.builder().maxConcurrency(60000).build()).build();
    }

}
