package com.extractor.as400.connector.connectors;

import com.extractor.as400.config.EnvironmentConfig;
import com.extractor.as400.util.ConfigVerification;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

public class SyslogConnector {
    private static SyslogIF syslogServer;

    public SyslogConnector() {
    }

    public static Object getConnector() throws Exception {

        if (SyslogConnector.syslogServer == null) {
            // Connect to the Syslog Server
            syslogServer = Syslog.getInstance(EnvironmentConfig.SYSLOG_PROTOCOL);
            syslogServer.getConfig().setHost(EnvironmentConfig.SYSLOG_HOST);
            syslogServer.getConfig().setPort(EnvironmentConfig.SYSLOG_PORT);
            System.out.println("***** " + ConfigVerification.getActualDate() + " Syslog Initiated *****");
        } else {
            System.out.println("***** "+ ConfigVerification.getActualDate() +" Syslog destination reused *****");
        }
        return syslogServer;
    }
}
