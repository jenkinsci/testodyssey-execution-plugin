package com.ekatechserv.eaf.plugin.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public enum Browser {

    IE("5587e5fc342d757bc13d489d"),
    FF("5587e5c3342d757bc13d489b"),
    CHROME("5587e5de342d757bc13d489c");

    private final String value;
    private static Map<String, String> browserMap;

    private Browser(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static String getDesc(String value) {
        switch (value) {
            case "5587e5fc342d757bc13d489d":
                return "Internet Explorer";
            case "5587e5c3342d757bc13d489b":
                return "Firefox";
            case "5587e5de342d757bc13d489c":
                return "Chrome";
            default:
                return null;
        }
    }

    public static Map<String, String> getBrowserMap() {
        initializeMap();
        return browserMap;
    }

    public static String getDescByCode(String code) {
        initializeMap();
        return browserMap.get(StringUtils.upperCase(code));
    }

    private static void initializeMap() {
        browserMap = new HashMap<>();
        for (Browser stepStatus : Browser.values()) {
            browserMap.put(stepStatus.getValue(), getDesc(stepStatus.getValue()));
        }
    }
}
