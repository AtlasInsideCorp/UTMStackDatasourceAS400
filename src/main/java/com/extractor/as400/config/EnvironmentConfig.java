package com.extractor.as400.config;

import com.extractor.as400.enums.EnvironmentsEnum;

public class EnvironmentConfig {
    // Connection parameters to AS400 (log origin)
    public static final String AS400_HOST_NAME = System.getenv(EnvironmentsEnum.AS400_HOST_NAME.getVarName());
    public static final String AS400_USER_ID = System.getenv(EnvironmentsEnum.AS400_USER_ID.getVarName());
    public static final String AS400_USER_PASSWORD = System.getenv(EnvironmentsEnum.AS400_USER_PASSWORD.getVarName());
    public static final String AS400_JSON_SERVERS_DEF = System.getenv(EnvironmentsEnum.AS400_JSON_SERVERS_DEF.getVarName());
    // Connection parameters to Syslog (log destination)
    public static final String SYSLOG_HOST = System.getenv(EnvironmentsEnum.SYSLOG_HOST.getVarName());

    private static String syslogPort = System.getenv(EnvironmentsEnum.SYSLOG_PORT.getVarName());
    public static final Integer SYSLOG_PORT = (syslogPort != null && syslogPort.trim().compareTo("") != 0 && syslogPort.matches("^\\d+$"))
            ?Integer.parseInt(syslogPort):514;
    public static final String SYSLOG_PROTOCOL = System.getenv(EnvironmentsEnum.SYSLOG_PROTOCOL.getVarName());

    // Every BATCH_SIZE logs the time mark is saved to file
    public static final int BATCH_SIZE = 100000;
    // Used as a key to identify as40 logs from third party tools
    public static final String META_AS400_KEY = "[DEVICE_TYPE=AS400, LOG_GEN_AGENT=U7M_574-CK] ";
}
