package com.extractor.as400.concurrent;

import com.extractor.as400.config.AS400ExtractorConstants;
import com.extractor.as400.config.InMemoryConfigurations;
import com.extractor.as400.connector.connectors.AS400Connector;
import com.extractor.as400.enums.ForwarderEnum;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.forwarders.ForwarderFactory;
import com.extractor.as400.interfaces.IForwarder;
import com.extractor.as400.models.ServerState;
import com.extractor.as400.util.ThreadsUtil;
import com.ibm.as400.access.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Freddy R. Laffita Almaguer
 * This class is used to execute log extraction from as400 in separated thread
 * Using this class you can extract logs simultaneously from many as400 systems
 * @see Thread#run()
 */
public class AS400IngestParallelTask implements Runnable {
    private static final String CLASSNAME = "AS400IngestParallelTask";
    private static final Logger logger = LogManager.getLogger(AS400IngestParallelTask.class);
    // Represents the server as400 that you are about to extract logs from
    ServerState serverState;
    // Represents the forwarder where to send logs
    ForwarderEnum forwarder;

    public AS400IngestParallelTask() {
    }

    public AS400IngestParallelTask withServerState(ServerState serverState) {
        this.serverState = serverState;
        return this;
    }

    public AS400IngestParallelTask withForwarder(ForwarderEnum forwarder) {
        this.forwarder = forwarder;
        return this;
    }

    public AS400IngestParallelTask build() {
        return this;
    }

    @Override
    public void run() {
        final String ctx = CLASSNAME + ".run";
        StringBuffer logsBuffer = new StringBuffer();
        ServerState serverState;
        String stateInfo;
        // Creating the list to store the messages batchs before sending to forwarder
        List<String> messagesForwardingList = new ArrayList<>(AS400ExtractorConstants.BATCH_SIZE * 2);

        try {
            // When the process launch, we first change the state to RUNNING
            InMemoryConfigurations.changeServerStateStatus(this.serverState, "RUNNING");
            serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
            stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
            logsBuffer.append("***** ").append(" Log extraction report from as400 ").append(stateInfo).append(" *****\n");

            serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
            stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
            logsBuffer.append("***** ").append(" PHASE 1. Connection ").append(stateInfo).append(" *****\n");

            // Connect to the AS400 server and Syslog destination
            AS400JPing pingObj = new AS400JPing(this.serverState.getServerDefAS400().getHostName());
            Object[] pingResult = pingVerification(pingObj);
            boolean pingStatusOk = (boolean) pingResult[0];
            String pingData = (String) pingResult[1];

            if (pingStatusOk) {
                // Begin sign-on test
                AS400 as400 = (AS400) new AS400Connector().getNewOrReuseConnector(this.serverState.getServerDefAS400());

                if (as400.authenticate(this.serverState.getServerDefAS400().getUserId(),
                        this.serverState.getServerDefAS400().getUserPassword().toCharArray())) {
                    // Log the services status
                    logsBuffer.append("***** SERVICES STATUS *******");
                    logsBuffer.append(pingData + "\n");
                    serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
                    stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
                    logsBuffer.append("***** ").append(" PHASE 2. Getting logs and sending to Syslog ").append(stateInfo).append(" *****\n");

                    // Getting logs
                    HistoryLog historyLog = null;
                    Enumeration messageList = null;
                    try {
                        historyLog = new HistoryLog(as400);
                        messageList = historyLog.getMessages();
                    } catch (Exception e) {
                        InMemoryConfigurations.changeServerStateStatus(this.serverState, "ERROR");
                        serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
                        stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
                        logsBuffer.append("***** ").append(" ERROR trying to get the HistoryLogs ").append(stateInfo).append(" *****\n");
                        logsBuffer.append("*** - Check your system version, must be higher than V5R4 *****\n");
                        logsBuffer.append("*** - Check if the HistoryLog exists and have some logs *****\n");
                        logsBuffer.append("*** - Check your system services availability and enable the failing services *****\n");
                        logsBuffer.append("*** - If your AS400 system is using default ports, check if the following ports are open: 8473, 8474, 8475, 8472, 8471, 446, 8470, 8476, 9473, 9474, 9475, 9472, 9471, 448, 9470, 9476 *****\n");
                        logsBuffer.append("*** - Check if you have access from the current IP to the AS400 system by ports above *****\n");

                        String nullMsg = "" + e.getMessage();
                        if (nullMsg.compareTo("null") == 0) {
                            nullMsg = nullMsg.replace("null", "The object returned is null");
                        }
                        logsBuffer.append("***** Message: ").append(nullMsg).append(" *****\n");
                    }

                    if (historyLog != null && messageList != null) {
                        logsBuffer.append("***** ").append(" Reading last state ").append(stateInfo).append(" *****\n");
                        // Read last saved state (log date)
                        long calendarSTART = FileOperations.readLastLogDate(this.serverState.getServerDefAS400());
                        serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
                        stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
                        logsBuffer.append("***** ").append(stateInfo).append(" Getting data From -> ").append(calendarSTART).append("\n");

                        // To store saved state
                        long calendarEND = 0L;
                        // Counter to count until BATCH_SIZE is reached
                        int batchCounter = 0;

                        while (messageList.hasMoreElements()) {
                            AS400Message message = (AS400Message) messageList.nextElement();
                            // We look for a time change after we reach the batch size because, in some environments, a bunch of logs can have the same time mark
                            // So we save the time mark after time change when we reach the BATCH_SIZE
                            if (batchCounter >= AS400ExtractorConstants.BATCH_SIZE && (calendarEND != message.getDate().getTimeInMillis())) {
                                if (calendarEND > calendarSTART) {
                                    FileOperations.saveLastLogDate(calendarEND, this.serverState.getServerDefAS400());
                                    batchCounter = 0;
                                    // Then send the logs to forwarder and clear the list
                                    IForwarder forwarder = new ForwarderFactory().getForwarder(this.forwarder, this.serverState);
                                    forwarder.forwardLogs(messagesForwardingList);
                                    messagesForwardingList.clear();
                                }
                            }
                            if (message.getDate().getTimeInMillis() > calendarSTART) {
                                messagesForwardingList.add(message.getText());
                                calendarEND = message.getDate().getTimeInMillis();
                                batchCounter++;
                            }

                        }

                        serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
                        stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
                        logsBuffer.append("***** ").append(" PHASE 3. Saving last log date ").append(stateInfo).append(" *****\n");


                        // Disconnecting from as400
                        historyLog.close();
                        as400.disconnectAllServices();

                        if (calendarEND > calendarSTART) {
                            FileOperations.saveLastLogDate(calendarEND, this.serverState.getServerDefAS400());
                        }
                        InMemoryConfigurations.changeServerStateStatus(this.serverState, "SUCCESS");
                        serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
                        stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
                        logsBuffer.append("***** ").append(" Process Result ").append(stateInfo).append(" *****\n");
                        logger.info(logsBuffer);
                        Thread.sleep(5000);

                        // End access to history log
                    } else {
                        logger.info(logsBuffer);
                    }
                    // End authentication attempt
                } else {
                    InMemoryConfigurations.changeServerStateStatus(this.serverState, "ERROR");
                    serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
                    stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
                    logsBuffer.append("***** ").append(" ERROR authentication attempt failed to -> ").append(stateInfo).append(" please, check you configuration at Servers.json file, or check the AS400 system, it may be unavailable at this moment *****\n");
                    logger.info(logsBuffer);
                }
                // End of ping test
            } else {
                InMemoryConfigurations.changeServerStateStatus(this.serverState, "ERROR");
                serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
                stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
                logsBuffer.append("***** ").append(" ERROR ping test failed to -> ").append(stateInfo).append(" system is unreachable, please check the options below: *****\n");
                logsBuffer.append("***** SERVICES STATUS *******");
                logsBuffer.append(pingData).append("\n");
                logsBuffer.append("*** - Check if the network is working and if you have access to the server from the current IP *****\n");
                logsBuffer.append("*** - Check if you are using a proxy between current IP and the AS400 *****\n");
                logsBuffer.append("*** - If your AS400 system is using default ports, check if the following ports are open: 8473, 8474, 8475, 8472, 8471, 446, 8470, 8476, 9473, 9474, 9475, 9472, 9471, 448, 9470, 9476 *****\n");
                logsBuffer.append("*** - Check if you have access from the current IP to the AS400 system by ports above *****\n");
                logsBuffer.append("*** - Enable the failing services *****\n");
                logger.info(logsBuffer);
            }

            //-----------------------------------------------------------------------------//
            // Wait some time before begin again
            ThreadsUtil.sleepCurrentThread(30);

        } catch (Exception e) {
            InMemoryConfigurations.changeServerStateStatus(this.serverState, "ERROR");
            serverState = InMemoryConfigurations.getServerStateStatus(this.serverState);
            stateInfo = serverState != null ? serverState.toString() : this.serverState.toString();
            logsBuffer.append("***** ").append(" ERROR getting data from as400 ").append(stateInfo).append(" *****\n");
            logsBuffer.append("***** Unable to access: ").append(e.getMessage()).append(" *****\n");
            logger.error(logsBuffer);

            // Wait some time before begin again
            ThreadsUtil.sleepCurrentThread(30);
            Thread.currentThread().interrupt();
        }
    }

    // Method to know services available
    public Object[] pingVerification(AS400JPing pingObj) {
        Object[] result = new Object[2];
        String pingStr = "";
        result[0] = true;
        result[1] = pingStr;

        if (pingObj.ping(AS400.PRINT)) {
            pingStr += "\n*** AS400.PRINT (SUCCESS) ***";
        } else {
            pingStr += "\n*** AS400.PRINT (FAIL) ***";
        }
        result[1] = pingStr;
        if (pingObj.ping(AS400.FILE)) {
            pingStr += "\n*** AS400.FILE (SUCCESS) ***";
        } else {
            pingStr += "\n*** AS400.FILE (FAIL) ***";
            result[0] = false;
        }
        result[1] = pingStr;
        if (pingObj.ping(AS400.COMMAND)) {
            pingStr += "\n*** AS400.COMMAND (SUCCESS) ***";
        } else {
            pingStr += "\n*** AS400.COMMAND (FAIL) ***";
            result[0] = false;
        }
        result[1] = pingStr;
        if (pingObj.ping(AS400.DATAQUEUE)) {
            pingStr += "\n*** AS400.DATAQUEUE (SUCCESS) ***";
        } else {
            pingStr += "\n*** AS400.DATAQUEUE (FAIL) ***";
            result[0] = false;
        }
        result[1] = pingStr;
        if (pingObj.ping(AS400.DATABASE)) {
            pingStr += "\n*** AS400.DATABASE (SUCCESS) ***";
        } else {
            pingStr += "\n*** AS400.DATABASE (FAIL) ***";
            result[0] = false;
        }
        result[1] = pingStr;
        if (pingObj.ping(AS400.RECORDACCESS)) {
            pingStr += "\n*** AS400.RECORDACCESS (SUCCESS) ***";
        } else {
            pingStr += "\n*** AS400.RECORDACCESS (FAIL) ***";
            result[0] = false;
        }
        result[1] = pingStr;
        if (pingObj.ping(AS400.CENTRAL)) {
            pingStr += "\n*** AS400.CENTRAL (SUCCESS) ***";
        } else {
            pingStr += "\n*** AS400.CENTRAL (FAIL) ***";
        }
        result[1] = pingStr;
        if (pingObj.ping(AS400.SIGNON)) {
            pingStr += "\n*** AS400.SIGNON (SUCCESS) ***";
        } else {
            pingStr += "\n*** AS400.SIGNON (FAIL) ***";
            result[0] = false;
        }
        result[1] = pingStr;


        return result;
    }

}
