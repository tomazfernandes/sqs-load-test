package io.awspring.cloud.sqs.awsloadtest.repository;

import io.awspring.cloud.sqs.awsloadtest.model.Run;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Tomaz Fernandes
 */
public interface RunRepository {

    Run save(Run run);

    Run findById(UUID runId);

    Collection<Run> findAll();

    Collection<Run> deleteAll();

    Run delete(UUID id);

}
