package io.awspring.cloud.sqs.awsloadtest.controller;

import io.awspring.cloud.sqs.awsloadtest.dto.RunCreateRequest;
import io.awspring.cloud.sqs.awsloadtest.dto.RunCreateResponse;
import io.awspring.cloud.sqs.awsloadtest.dto.RunGetResponse;
import io.awspring.cloud.sqs.awsloadtest.model.Run;
import io.awspring.cloud.sqs.awsloadtest.service.RunService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<RunCreateResponse> create(@Valid @RequestBody RunCreateRequest request) {
        Run startedRun = runService.createAndStart(request.toEntity());
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(startedRun.id())
                .toUri();
        return ResponseEntity.created(uri).body(RunCreateResponse.of(startedRun, uri));
    }

    @GetMapping("/{id}")
    public RunGetResponse get(@PathVariable("id") UUID id) {
        return RunGetResponse.of(runService.findById(id));
    }

    @GetMapping
    public Collection<RunGetResponse> getAll() {
        return runService.findAll().stream().map(RunGetResponse::of).collect(Collectors.toList());
    }

    @DeleteMapping
    public Collection<RunGetResponse> deleteAll() {
        return runService.deleteAll().stream().map(RunGetResponse::of).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public RunGetResponse delete(@PathVariable("id") UUID id) {
        return RunGetResponse.of(runService.delete(id));
    }

}
