package com.extractor.as400;

import java.util.Enumeration;
import com.extractor.as400.connector.factory.ConnectorFactory;
import com.extractor.as400.enums.ConnectorEnum;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.util.ConfigVerification;
import com.ibm.as400.access.*;
import org.productivity.java.syslog4j.SyslogIF;


public class Main {
    public static final int BATCH_SIZE = 100000;
    public static final String META_AS400_KEY = "[DEVICE_TYPE=AS400, LOG_GEN_AGENT=U7M_574-CK] ";
    public static void main(String[] args) {

        // Set always running
        while (true) {
            // Check if environment is ok
           if (ConfigVerification.isEnvironmentOk()) {
               try {

                   System.out.println("***** PHASE 1. Connection *****");
                   // Connect to the AS400 server and Syslog destination
                   AS400 as400 = (AS400) new ConnectorFactory().getConnectorByType(ConnectorEnum.AS400_V1.get()).getConnector();
                   SyslogIF syslogServer = (SyslogIF) new ConnectorFactory().getConnectorByType(ConnectorEnum.SYSLOG_V1.get()).getConnector();


                   System.out.println("***** PHASE 2. Getting logs and sending to Syslog *****");
                   // Getting logs
                   HistoryLog historyLog = new HistoryLog(as400);
                   Enumeration messageList = historyLog.getMessages();

                   // Read last saved state (log date)
                   long calendarSTART = FileOperations.readLastLogDate();
                   System.out.println("***** From -> " + calendarSTART);

                   // To store saved state
                   long calendarEND = 0L;
                   // Counter to count until BATCH_SIZE is reached
                   int batchCounter = 0;

                   while (messageList.hasMoreElements()) {
                       AS400Message message = (AS400Message) messageList.nextElement();
                       // We look for a time change after we reach the batch size because, in some environments, a bunch of logs can have the same time mark
                       // So we save the time mark after time change when we reach the BATCH_SIZE
                       if (batchCounter >= BATCH_SIZE && (calendarEND != message.getDate().getTimeInMillis())) {
                           if (calendarEND > calendarSTART) {
                               FileOperations.saveLastLogDate(calendarEND);
                           }
                       } else {
                           if (message.getDate().getTimeInMillis() > calendarSTART) {
                               syslogServer.log(syslogServer.getConfig().getFacility(), META_AS400_KEY+message.getText());
                               calendarEND = message.getDate().getTimeInMillis();
                               batchCounter++;
                           }
                       }
                   }
                   System.out.println("***** PHASE 3. Saving last log date *****");
                   historyLog.close();
                   if (calendarEND > calendarSTART) {
                       FileOperations.saveLastLogDate(calendarEND);
                   }
                   Thread.sleep(5000);


               } catch (Exception ex) {
                   System.out.println(ex.getMessage());
               }
           }
        }
    }
}