package io.awspring.cloud.sqs.awsloadtest.repository;

import io.awspring.cloud.sqs.awsloadtest.model.Run;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tomaz Fernandes
 * @since 3.0
 */
@Repository
public class InMemoryRunRepository implements RunRepository {

    private final Map<UUID, Run> runs = new ConcurrentHashMap<>();

    public Run save(Run run) {
        runs.put(run.id(), run);
        return run;
    }

    public Run findById(UUID runId) {
        return runs.get(runId);
    }

    @Override
    public Collection<Run> findAll() {
        return runs.values();
    }

    @Override
    public Collection<Run> deleteAll() {
        Collection<Run> deletedRuns = runs.values();
        runs.clear();
        return deletedRuns;
    }

    @Override
    public Run delete(UUID id) {
        return runs.remove(id);
    }

}
