package com.extractor.as400;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.extractor.as400.concurrent.AS400ParallelTask;
import com.extractor.as400.connector.connectors.SyslogConnector;
import com.extractor.as400.models.ServerState;
import com.extractor.as400.util.ConfigVerification;
import org.productivity.java.syslog4j.SyslogIF;


public class Main {

    public static void main(String[] args) {
        System.out.println("**************************************************************");
        System.out.println("***** DATASOURCE AS400 VERSION " + ConfigVerification.API_VERSION + " *****");
        System.out.println("**************************************************************");
        // Check if environment is ok
        if (ConfigVerification.isEnvironmentOk()) {

            //First we create fixed thread pool executor with (Servers count) threads, one per server
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ConfigVerification.getServerStateList().size());

            //Creating syslog connection (Destination)
            SyslogIF syslogServer = null;
            try {
                syslogServer = (SyslogIF) SyslogConnector.getConnector();
            } catch (Exception e) {
                e.printStackTrace();
            }


            //--------------------------------The concurrent ETL process is here-------------------------------------------
            if (syslogServer != null) {
                Iterator<ServerState> serverStateIterator;
                for (serverStateIterator = ConfigVerification.getServerStateList().iterator(); serverStateIterator.hasNext(); ) {
                    try {
                        executor.execute(new AS400ParallelTask(serverStateIterator.next(), syslogServer));
                    } catch (Exception e) {

                    }
                }

                //Thread end is called
                executor.shutdown();
                //Wait 1 sec until termination
                while (!executor.isTerminated()) {
                    try {
                        executor.awaitTermination(1, TimeUnit.SECONDS);
                    } catch (Exception e) {

                    }
                }

                // Finally, print summary of servers status
                Iterator<ServerState> summaryIterator;
                System.out.println("***** " + ConfigVerification.getActualDate() + " SERVERS SUMMARY *****");
                for (summaryIterator = ConfigVerification.getServerStateList().iterator(); summaryIterator.hasNext(); ) {
                    ServerState tmp = summaryIterator.next();
                    System.out.println("***** " + tmp.toString() + " *****");
                }
                System.out.println("***** SERVERS SUMMARY (END) *****");
            } else {
                System.out.println("***** ERROR. For some reasons, the syslog server can't be created, please check your environment and network *****");
            }
        }
        // Wait 5 seconds to have time to stop if using docker compose with restart always
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
    }

}