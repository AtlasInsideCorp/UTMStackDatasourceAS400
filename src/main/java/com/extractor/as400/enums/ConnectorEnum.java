package com.extractor.as400.enums;

public enum ConnectorEnum {
    AS400_V1("AS400_V1"),
    SYSLOG_V1("SYSLOG_V1");

    private String varName;

    ConnectorEnum(String varName) {
        this.varName = varName;
    }

    public String get() {
        return varName;
    }
}
