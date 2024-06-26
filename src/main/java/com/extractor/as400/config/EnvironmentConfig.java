package com.extractor.as400.config;

import com.extractor.as400.enums.EnvironmentsEnum;

public class EnvironmentConfig {
    // Connection parameters to AS400 (log origin)
    public static final String SYSLOG_HOST = System.getenv(EnvironmentsEnum.SYSLOG_HOST.getVarName());

    private static String syslogPort = System.getenv(EnvironmentsEnum.SYSLOG_PORT.getVarName());
    public static final Integer SYSLOG_PORT = (syslogPort != null && syslogPort.trim().compareTo("") != 0 && syslogPort.matches("^\\d+$"))
            ?Integer.parseInt(syslogPort):514;
    public static final String SYSLOG_PROTOCOL = System.getenv(EnvironmentsEnum.SYSLOG_PROTOCOL.getVarName());
}
