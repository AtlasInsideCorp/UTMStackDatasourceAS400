package com.extractor.as400.forwarders.impl;

import com.extractor.as400.concurrent.AS400IngestParallelTask;
import com.extractor.as400.config.AS400ExtractorConstants;
import com.extractor.as400.connector.connectors.SyslogConnector;
import com.extractor.as400.interfaces.IForwarder;
import com.extractor.as400.models.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.productivity.java.syslog4j.SyslogIF;

import java.util.List;

/**
 * @author Freddy R. Laffita Almaguer
 * This class is used to forward extracted AS400 messages to syslog
 */
public class SyslogForwarder implements IForwarder {

    private static final String CLASSNAME = "SyslogForwarder";
    private static final Logger logger = LogManager.getLogger(SyslogForwarder.class);

    private ServerState as400Server;
    public SyslogForwarder withServerState (ServerState as400Server) {
        this.as400Server = as400Server;
        return this;
    }
    public SyslogForwarder build () {
        return this;
    }

    /**
     * @param messages is the list of messages to send
     * @return a boolean representing the final state of the process
     */
    @Override
    public boolean forwardLogs(List<String> messages) {
        final String ctx = CLASSNAME + ".forwardLogs";
        //Creating syslog connection (Destination)
        SyslogIF syslogServer;
        try {
            syslogServer = (SyslogIF) SyslogConnector.getConnector();
            SyslogIF finalSyslogServer = syslogServer;

            messages.forEach(m-> finalSyslogServer.log(finalSyslogServer.getConfig().getFacility(),
                    AS400ExtractorConstants.META_AS400_KEY + "[AS400Server=" + this.as400Server.getServerDefAS400().getHostName() + "] "
                            + "[AS400Tenant=" + this.as400Server.getServerDefAS400().getTenant() + "] " + m));

        } catch (Exception e) {
            logger.error(ctx + ": " + e.getMessage());
            return false;
        }
        return true;
    }
}
