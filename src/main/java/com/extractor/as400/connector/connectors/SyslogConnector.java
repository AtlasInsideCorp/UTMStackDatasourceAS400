package com.extractor.as400.connector.connectors;

import com.extractor.as400.config.EnvironmentConfig;
import com.extractor.as400.util.ConfigVerification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

/**
 * @author Freddy R. Laffita Almaguer
 * This class is used to get a singleton SyslogConnector
 */
public class SyslogConnector {
    private static SyslogIF syslogServer;
    private static final String CLASSNAME = "SyslogConnector";
    private static final Logger logger = LogManager.getLogger(SyslogConnector.class);

    public SyslogConnector() {
    }

    public static Object getConnector() throws Exception {

        if (SyslogConnector.syslogServer == null) {
            // Connect to the Syslog Server
            syslogServer = Syslog.getInstance(EnvironmentConfig.SYSLOG_PROTOCOL);
            syslogServer.getConfig().setHost(EnvironmentConfig.SYSLOG_HOST);
            syslogServer.getConfig().setPort(EnvironmentConfig.SYSLOG_PORT);
            logger.info("***** " + ConfigVerification.getActualDate() + " Syslog Initiated *****");
        } else {
            logger.info("***** "+ ConfigVerification.getActualDate() +" Syslog destination reused *****");
        }
        return syslogServer;
    }
}
