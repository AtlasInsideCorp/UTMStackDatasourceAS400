package com.extractor.as400.concurrent;

import com.extractor.as400.config.EnvironmentConfig;
import com.extractor.as400.connector.connectors.AS400Connector;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.models.ServerState;
import com.extractor.as400.util.ConfigVerification;
import com.ibm.as400.access.*;
import org.productivity.java.syslog4j.SyslogIF;
import java.util.Enumeration;

public class AS400ParallelTask implements Runnable {
    /**
     * This class is used to execute log extraction from as400 in separated thread
     * Using this class you can extract logs simultaneously from many as400 systems
     *
     * @see Thread#run()
     */
    // Represents the server as400 that you are about to extract logs from
    ServerState serverState;
    // Syslog server (Destination)
    SyslogIF syslogServer;

    public AS400ParallelTask(ServerState serverState, SyslogIF syslogServer) {
        this.serverState = serverState;
        this.syslogServer = syslogServer;
    }

    @Override
    public void run() {
        StringBuffer logsBuffer = new StringBuffer();
        // When the process launch, we first change the state to RUNNING
        ConfigVerification.changeServerStateStatus(this.serverState, "RUNNING");
        logsBuffer.append("***** " + ConfigVerification.getActualDate() + " Log extraction report from as400 " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " *****\n");
        try {

            logsBuffer.append("***** " + ConfigVerification.getActualDate() + " PHASE 1. Connection " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " *****\n");

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

                        logsBuffer.append("***** " + ConfigVerification.getActualDate() + " PHASE 2. Getting logs and sending to Syslog " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " *****\n");
                        // Getting logs
                        HistoryLog historyLog = new HistoryLog(as400);
                        Enumeration messageList = historyLog.getMessages();

                        // Read last saved state (log date)
                        long calendarSTART = FileOperations.readLastLogDate(this.serverState.getServerDefAS400());
                        logsBuffer.append("***** " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " Getting data From -> " + calendarSTART + "\n");

                        // To store saved state
                        long calendarEND = 0L;
                        // Counter to count until BATCH_SIZE is reached
                        int batchCounter = 0;

                        while (messageList.hasMoreElements()) {
                            AS400Message message = (AS400Message) messageList.nextElement();
                            // We look for a time change after we reach the batch size because, in some environments, a bunch of logs can have the same time mark
                            // So we save the time mark after time change when we reach the BATCH_SIZE
                            if (batchCounter >= EnvironmentConfig.BATCH_SIZE && (calendarEND != message.getDate().getTimeInMillis())) {
                                if (calendarEND > calendarSTART) {
                                    FileOperations.saveLastLogDate(calendarEND, this.serverState.getServerDefAS400());
                                }
                            } else {
                                if (message.getDate().getTimeInMillis() > calendarSTART) {
                                    this.syslogServer.log(this.syslogServer.getConfig().getFacility(),
                                            EnvironmentConfig.META_AS400_KEY + "[AS400Server=" + this.serverState.getServerDefAS400().getHostName() + "] " + message.getText());
                                    calendarEND = message.getDate().getTimeInMillis();
                                    batchCounter++;
                                }
                            }
                        }

                        logsBuffer.append("***** " + ConfigVerification.getActualDate() + " PHASE 3. Saving last log date " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " *****\n");


                        // Disconnecting from as400
                        historyLog.close();
                        as400.disconnectAllServices();

                        if (calendarEND > calendarSTART) {
                            FileOperations.saveLastLogDate(calendarEND, this.serverState.getServerDefAS400());
                        }
                        ConfigVerification.changeServerStateStatus(this.serverState, "SUCCESS");
                        logsBuffer.append("***** " + ConfigVerification.getActualDate() + " Process Result " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " *****\n");
                        System.out.println(logsBuffer);
                        Thread.sleep(5000);

                        // End authentication attempt
                    } else {
                        ConfigVerification.changeServerStateStatus(this.serverState, "ERROR");
                        logsBuffer.append("***** " + ConfigVerification.getActualDate() + " ERROR authentication attempt failed to -> " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " please, check you configuration at Servers.json file, or check the AS400 system, it may be unavailable at this moment *****\n");
                        System.out.println(logsBuffer);
                    }
                // End of ping test
            } else {
                ConfigVerification.changeServerStateStatus(this.serverState, "ERROR");
                logsBuffer.append("***** " + ConfigVerification.getActualDate() + " ERROR ping test failed to -> " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " system is unreachable, please check the options below: *****\n");
                logsBuffer.append("***** SERVICES STATUS *******");
                logsBuffer.append(pingData+"\n");
                logsBuffer.append("*** - Check if the network is working and if you have access to the server from the current IP *****\n");
                logsBuffer.append("*** - Check if you are using a proxy between current IP and the AS400 *****\n");
                logsBuffer.append("*** - If your AS400 system is using default ports, check if the following ports are open: 8473, 8474, 8475, 8472, 8471, 446, 8470, 8476, 9473, 9474, 9475, 9472, 9471, 448, 9470, 9476 *****\n");
                logsBuffer.append("*** - Check if you have access from the current IP to the AS400 system by ports above *****\n");
                logsBuffer.append("*** - Enable the failing services *****\n");
                System.out.println(logsBuffer);
            }

        } catch (Exception e) {
            ConfigVerification.changeServerStateStatus(this.serverState, "ERROR");
            logsBuffer.append("***** " + ConfigVerification.getActualDate() + " ERROR getting data from as400 " + ConfigVerification.getServerStateStatus(this.serverState).toString() + " *****\n");
            logsBuffer.append("***** Message: " + e.getMessage() + " *****\n");
            System.out.println(logsBuffer);
            Thread.currentThread().interrupt();
        }
    }

    // Method to know services available
    public Object [] pingVerification(AS400JPing pingObj) throws Exception {
        Object[] result = new Object[2];
        String pingStr = "";
        boolean pingBol = true;
        result[0] = pingBol;
        result[1] = pingStr;

        if (pingObj.ping(AS400.PRINT)) {
            pingStr+="\n*** AS400.PRINT (SUCCESS) ***";
            result[1] = pingStr;
        } else {
            pingStr+="\n*** AS400.PRINT (FAIL) ***";
            result[1] = pingStr;
        }
        if (pingObj.ping(AS400.FILE)) {
            pingStr+="\n*** AS400.FILE (SUCCESS) ***";
            result[1] = pingStr;
        } else {
            pingStr+="\n*** AS400.FILE (FAIL) ***";
            result[0] = false;
            result[1] = pingStr;
        }
        if (pingObj.ping(AS400.COMMAND)) {
            pingStr+="\n*** AS400.COMMAND (SUCCESS) ***";
            result[1] = pingStr;
        } else {
            pingStr+="\n*** AS400.COMMAND (FAIL) ***";
            result[0] = false;
            result[1] = pingStr;
        }
        if (pingObj.ping(AS400.DATAQUEUE)) {
            pingStr+="\n*** AS400.DATAQUEUE (SUCCESS) ***";
            result[1] = pingStr;
        } else {
            pingStr+="\n*** AS400.DATAQUEUE (FAIL) ***";
            result[0] = false;
            result[1] = pingStr;
        }
        if (pingObj.ping(AS400.DATABASE)) {
            pingStr+="\n*** AS400.DATABASE (SUCCESS) ***";
            result[1] = pingStr;
        } else {
            pingStr+="\n*** AS400.DATABASE (FAIL) ***";
            result[0] = false;
            result[1] = pingStr;
        }
        if (pingObj.ping(AS400.RECORDACCESS)) {
            pingStr+="\n*** AS400.RECORDACCESS (SUCCESS) ***";
            result[1] = pingStr;
        } else {
            pingStr+="\n*** AS400.RECORDACCESS (FAIL) ***";
            result[0] = false;
            result[1] = pingStr;
        }
        if (pingObj.ping(AS400.CENTRAL)) {
            pingStr+="\n*** AS400.CENTRAL (SUCCESS) ***";
            result[1] = pingStr;
        } else {
            pingStr+="\n*** AS400.CENTRAL (FAIL) ***";
            result[1] = pingStr;
        }
        if (pingObj.ping(AS400.SIGNON)) {
            pingStr+="\n*** AS400.SIGNON (SUCCESS) ***";
            result[1] = pingStr;
        } else {
            pingStr+="\n*** AS400.SIGNON (FAIL) ***";
            result[0] = false;
            result[1] = pingStr;
        }


        return result;
    }

}
