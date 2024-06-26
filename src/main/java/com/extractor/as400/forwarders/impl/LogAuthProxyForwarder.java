package com.extractor.as400.forwarders.impl;

import agent.CollectorOuterClass.CollectorModule;
import agent.Common.ConnectorType;
import com.extractor.as400.config.AS400ExtractorConstants;
import com.extractor.as400.enums.ForwarderEnum;
import com.extractor.as400.enums.ValidationTypeEnum;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.interfaces.IForwarder;
import com.extractor.as400.models.ServerState;
import com.extractor.as400.util.Validations;
import com.utmstack.grpc.connection.GrpcConnection;
import com.utmstack.grpc.exception.LogMessagingException;
import com.utmstack.grpc.jclient.config.Constants;
import com.utmstack.grpc.jclient.config.interceptors.KeyStore;
import com.utmstack.grpc.jclient.config.interceptors.impl.GrpcKeyInterceptor;
import com.utmstack.grpc.service.LogMessagingService;
import logservice.Log.LogMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LogAuthProxyForwarder implements IForwarder {

    private static final String CLASSNAME = "LogAuthProxyForwarder";
    private static final Logger logger = LogManager.getLogger(LogAuthProxyForwarder.class);
    private static final int FORWARDER_BATCH_SIZE = ForwarderEnum.GRPC_LOG_AUTH_PROXY.getfBatchSize();

    private ServerState as400Server;

    public LogAuthProxyForwarder withServerState(ServerState as400Server) {
        this.as400Server = as400Server;
        return this;
    }

    public LogAuthProxyForwarder build() {
        return this;
    }

    /**
     * @param messages is the list of messages to send
     * @return a boolean representing the final state of the process
     */
    @Override
    public boolean forwardLogs(List<String> messages) {
        final String ctx = CLASSNAME + ".forwardLogs";
        AtomicInteger countDown = new AtomicInteger(0);

        try {
            // Begin gRPC connection
            Map<String, String> info = FileOperations.getCollectorInfo();
            String collectorManagerHost = info.get(AS400ExtractorConstants.COLLECTOR_MANAGER_HOST);
            String collectorLogsPort = info.get(AS400ExtractorConstants.COLLECTOR_LOGS_PORT);
            String connectionKey = info.get(Constants.COLLECTOR_KEY_HEADER);

            // Set the authentication needed to forward logs through gRPC
            KeyStore.setConnectionKey(connectionKey);
            // Connectiong to gRPC server
            GrpcConnection con = new GrpcConnection();
            con.createChannel(collectorManagerHost,
                    Validations.validateNumber(collectorLogsPort, ValidationTypeEnum.PORT),
                    new GrpcKeyInterceptor());

            // Calling the service
            LogMessagingService serv = new LogMessagingService(con);

            // Getting collector info
            String collectorKey = info.get(Constants.COLLECTOR_KEY_HEADER);

            // Creating the batch list to store the messages to send according to the forwarder batch size
            List<String> grpcBatchList = new ArrayList<>();

            // Iterating over messages and send batch through gRPC
            messages.forEach(m -> {
                if (countDown.get() < FORWARDER_BATCH_SIZE) {
                    grpcBatchList.add(formatLog(m));
                    countDown.addAndGet(1);
                } else {
                    LogMessage messageBatch = LogMessage.newBuilder().addAllData(grpcBatchList)
                            .setLogType(CollectorModule.AS_400.name()).setType(ConnectorType.COLLECTOR).build();
                    try {
                        countDown.set(0);
                        serv.sendLogs(messageBatch, collectorKey);
                        grpcBatchList.clear();
                    } catch (LogMessagingException e) {
                        grpcBatchList.clear();
                        countDown.set(0);
                        throw new RuntimeException("Unable to send log batch from: " + this.as400Server.getServerDefAS400().getHostName() + " -> "+e.getMessage());
                    }
                    // Add the message number 101
                    grpcBatchList.add(formatLog(m));
                    countDown.addAndGet(1);
                }
            });

            // Finally send the remaining logs in the list
            if (!grpcBatchList.isEmpty()) {
                LogMessage messageBatch = LogMessage.newBuilder().addAllData(grpcBatchList)
                        .setLogType(CollectorModule.AS_400.name()).setType(ConnectorType.COLLECTOR).build();
                try {
                    countDown.set(0);
                    serv.sendLogs(messageBatch, collectorKey);
                    grpcBatchList.clear();
                } catch (LogMessagingException e) {
                    grpcBatchList.clear();
                    countDown.set(0);
                    throw new RuntimeException("Unable to send log batch from: " + this.as400Server.getServerDefAS400().getHostName() + " -> " + e.getMessage());
                }
            }
            // Close the connection channel
            con.getConnectionChannel().shutdown();
        } catch (Exception e) {
            logger.error(ctx + ": " + e.getMessage());
            return false;
        }
        return true;
    }
    /**
     * Method used to format the input string to the log-auth-proxy standard
     * */
    public String formatLog (String logLine) {
        return "[utm_stack_collector_ds=" + this.as400Server.getServerDefAS400().getHostName() + "]-"+ AS400ExtractorConstants.META_AS400_KEY + logLine;
    }
}
