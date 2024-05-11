package com.extractor.as400.enums;

public enum ConfigurationParamsEnum {
    AS400_USER("collector.as400.user"),
    AS400_PASS("collector.as400.password"),
    AS400_HOST("collector.as400.hostname");

    private String value;
    ConfigurationParamsEnum (String value) {
        this.value = value;
    }

    public String get() {
        return value;
    }
}
