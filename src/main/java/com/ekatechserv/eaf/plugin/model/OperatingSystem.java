package com.ekatechserv.eaf.plugin.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public enum OperatingSystem {
    WIN2008("559e9955ca1f05adcaff70c6"),
    WIN2012("559e996aca1f05adcaff70c8"),
    LINUX("564d745acfc97f939fea4a56");

    private final String value;
    private static Map<String, String> osMap;

    private OperatingSystem(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static String getDesc(String value) {
        switch (value) {
            case "559e9955ca1f05adcaff70c6":
                return "Windows Server 2008 R2";
            case "559e996aca1f05adcaff70c8":
                return "Windows Server 2012 R2";
            case "564d745acfc97f939fea4a56":
                return "Linux Ubuntu-14.04";
            default:
                return null;
        }
    }

    public static Map<String, String> getOsMap() {
        initializeMap();
        return osMap;
    }

    public static String getDescByCode(String code) {
        initializeMap();
        return osMap.get(StringUtils.upperCase(code));
    }

    private static void initializeMap() {
        osMap = new HashMap<>();
        for (OperatingSystem stepStatus : OperatingSystem.values()) {
            osMap.put( stepStatus.getValue(),getDesc(stepStatus.getValue()));
        }
    }
}
