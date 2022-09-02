package io.awspring.cloud.sqs.awsloadtest.controller;

import io.awspring.cloud.sqs.awsloadtest.dto.RunCreateRequest;
import io.awspring.cloud.sqs.awsloadtest.dto.RunCreateResponse;
import io.awspring.cloud.sqs.awsloadtest.service.RunService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author Tomaz Fernandes
 */
@RestController
@RequestMapping("/v1/runs")
public class RunController {

    private final RunService runService;

    public RunController(RunService loadTestService) {
        this.runService = loadTestService;
    }

    @PostMapping
    public RunCreateResponse create(@Valid @RequestBody RunCreateRequest request) {
        return RunCreateResponse.of(runService.createAndStart(request.toEntity()));
    }

}
