package com.microsoftTeams.bot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * class for storing build related information from jenkins
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "buildInfo")
@SuppressWarnings("unused")
public class BuildInfo {

    @Id
    private String id;
    private String buildStatus;
    private int buildNumber;
    private String buildUrl;
    private String buildUser;
    private String buildUserEmail;
    private String buildJobName;
    private Map<String, String> parameters;
    private Long buildStartTime;
    private Long buildEndTime;

    // Constructor
    public BuildInfo() {}

    // Getters and Setters
    public String getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public void setBuildUrl(String buildUrl) {
        this.buildUrl = buildUrl;
    }

    public String getBuildUser() {
        return buildUser;
    }

    public void setBuildUser(String buildUser) {
        this.buildUser = buildUser;
    }

    public String getBuildUserEmail() {
        return buildUserEmail;
    }

    public void setBuildUserEmail(String buildUserEmail) {
        this.buildUserEmail = buildUserEmail;
    }

    public String getBuildJobName() {
        return buildJobName;
    }

    public void setBuildJobName(String buildJobName) {
        this.buildJobName = buildJobName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Long getBuildStartTime() {
        return buildStartTime;
    }

    public void setBuildStartTime(Long buildStartTime) {
        this.buildStartTime = buildStartTime;
    }

    public Long getBuildEndTime() {
        return buildEndTime;
    }

    public void setBuildEndTime(Long buildEndTime) {
        this.buildEndTime = buildEndTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


