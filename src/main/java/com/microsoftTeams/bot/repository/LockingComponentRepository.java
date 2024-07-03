package com.microsoftTeams.bot.repository;

import com.microsoftTeams.bot.models.LockingComponent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LockingComponentRepository extends MongoRepository<LockingComponent, String> {
    LockingComponent findByComponent(String component);

    @Query(value = "{'component': {$exists: true}}", fields = "{'component': 1, '_id': 0}")
    List<String> findAllComponentStrings();

}
