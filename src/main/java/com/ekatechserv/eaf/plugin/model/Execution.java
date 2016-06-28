package com.ekatechserv.eaf.plugin.model;

import java.io.Serializable;
import java.util.List;

public class Execution implements Serializable{

    private String _id;
    private String name;
    private String executionType;
    private String projectId;
    private String description;
    private String baseUrl;
    private String userId;
    private String orgShortCode;
    private List<EnvModMapper> envMods;

    public Execution(String name) {
        this.name = name;
    }
    
    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutionType() {
        return executionType;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgShortCode() {
        return orgShortCode;
    }

    public void setOrgShortCode(String orgShortCode) {
        this.orgShortCode = orgShortCode;
    }

    public List<EnvModMapper> getEnvMods() {
        return envMods;
    }

    public void setEnvMods(List<EnvModMapper> envMods) {
        this.envMods = envMods;
    }

}
