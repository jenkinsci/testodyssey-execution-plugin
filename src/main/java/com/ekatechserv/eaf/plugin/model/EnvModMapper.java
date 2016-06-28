package com.ekatechserv.eaf.plugin.model;

import java.io.Serializable;
import java.util.List;

public class EnvModMapper implements Serializable{

    public static final String FIELD_OS = "osId";
    public static final String FIELD_BROWSER = "browserId";
    public static final String FIELD_MODULES = "modules";
    public static final String FIELD_BASEURL = "baseUrl";

    private String osId;

    private String browserId;

    private List<String> modules;

    private String baseUrl;

    public EnvModMapper(String osId, String browserId, String baseUrl) {
        this.osId = osId;
        this.browserId = browserId;
        this.baseUrl = baseUrl;
    }

    public String getOsId() {
        return osId;
    }

    public void setOsId(String osId) {
        this.osId = osId;
    }

    public String getBrowserId() {
        return browserId;
    }

    public void setBrowserId(String browserId) {
        this.browserId = browserId;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
