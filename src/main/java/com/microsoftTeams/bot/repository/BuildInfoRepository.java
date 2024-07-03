package com.microsoftTeams.bot.repository;

import com.microsoftTeams.bot.models.BuildInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BuildInfoRepository extends MongoRepository<BuildInfo, String> {
    BuildInfo findByBuildJobNameAndBuildNumber(String buildJobName, int buildNumber);
    List<BuildInfo> findByBuildJobNameAndBuildStartTimeGreaterThanOrBuildEndTimeGreaterThan(String jobName, Long startTime, Long endTime);

}
