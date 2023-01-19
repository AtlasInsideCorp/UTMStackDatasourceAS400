package com.extractor.as400.connector.connectors;

import com.extractor.as400.config.EnvironmentConfig;
import com.extractor.as400.interfaces.IConnector;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

public class SyslogConnector implements IConnector {
    private static SyslogIF syslogServer;

    public SyslogConnector() {
    }

    @Override
    public Object getConnector() throws Exception {

        if (syslogServer == null) {
            // Connect to the Syslog Server
            syslogServer = Syslog.getInstance(EnvironmentConfig.SYSLOG_PROTOCOL);
            syslogServer.getConfig().setHost(EnvironmentConfig.SYSLOG_HOST);
            syslogServer.getConfig().setPort(EnvironmentConfig.SYSLOG_PORT);
            System.out.println("***** Syslog Initiated *****");
        } else {
            System.out.println("***** Syslog destination reused *****");
        }
        return syslogServer;
    }
}
