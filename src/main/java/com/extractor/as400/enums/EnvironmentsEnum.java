package com.extractor.as400.enums;

public enum EnvironmentsEnum {
    AS400_HOST_NAME("AS400_HOST_NAME"),
    AS400_USER_ID("AS400_USER_ID"),
    AS400_USER_PASSWORD("AS400_USER_PASSWORD"),
    AS400_JSON_SERVERS_DEF("AS400_JSON_SERVERS_DEF"),
    SYSLOG_HOST("SYSLOG_HOST"),
    SYSLOG_PORT("SYSLOG_PORT"),
    SYSLOG_PROTOCOL("SYSLOG_PROTOCOL");

    private String varName;

    EnvironmentsEnum(String varName) {
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }
}
