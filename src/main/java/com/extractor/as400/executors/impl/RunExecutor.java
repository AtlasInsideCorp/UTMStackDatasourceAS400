package com.extractor.as400.executors.impl;

import com.extractor.as400.concurrent.AS400PingParallelTask;
import com.extractor.as400.concurrent.AS400IngestParallelTask;
import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.ForwarderEnum;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.interfaces.IExecutor;
import com.extractor.as400.models.ServerState;
import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.util.ThreadsUtil;
import com.extractor.as400.util.UsageHelp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to perform the actions when running the collector
 * Ping requests to collector manager, load configurations when changes
 * occours and send logs to collector manager
 */
public class RunExecutor implements IExecutor {

    private static final String CLASSNAME = "RunExecutor";
    private static final Logger logger = LogManager.getLogger(RunExecutor.class);

    /**
     * @throws ExecutorAS400Exception
     */
    @Override
    public void execute() throws ExecutorAS400Exception {
        final String ctx = CLASSNAME + ".execute";
        // Check if the lock file exists before executing the installer
        if (FileOperations.isLockFileCreated()) {
            //Create other fixed executor to hold the ping requests
            ThreadPoolExecutor pingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            //Launch ping thread
            try {
                pingExecutor.execute(new AS400PingParallelTask());
            } catch (Exception e) {
                logger.error(ctx + ": " + e.getMessage());
            }

            //--------------------------------The concurrent ETL process is here-------------------------------------------
            boolean needConfigUpdate = false;
            while (true) {
                if (!needConfigUpdate) {
                    if (ConfigVerification.isEnvironmentOk()) {
                        //First we create fixed thread pool executor with (Servers count) threads, one per server
                        ThreadPoolExecutor etlExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ConfigVerification.getServerStateList().size());

                        Iterator<ServerState> serverStateIterator;
                        for (serverStateIterator = ConfigVerification.getServerStateList().iterator(); serverStateIterator.hasNext(); ) {
                            try {
                                etlExecutor.execute(new AS400IngestParallelTask().withServerState(serverStateIterator.next())
                                        .withForwarder(ForwarderEnum.getByValue((String) UsageHelp.getParamsFromArgs()
                                                .get(AllowedParamsEnum.PARAM_OUTPUT_FORWARDER.get()))).build());
                            } catch (Exception e) {
                                logger.error(ctx + ": Error processing logs -> " + e.getMessage());
                            }
                        }

                        // Stopping executors
                        ThreadsUtil.stopThreadExecutor(etlExecutor);

                        // Finally, print summary of servers status
                        Iterator<ServerState> summaryIterator;
                        System.out.println("***** " + ConfigVerification.getActualDate() + " SERVERS SUMMARY *****");
                        for (summaryIterator = ConfigVerification.getServerStateList().iterator(); summaryIterator.hasNext(); ) {
                            ServerState tmp = summaryIterator.next();
                            System.out.println("***** " + tmp.toString() + " *****");
                        }
                        System.out.println("***** SERVERS SUMMARY (END) *****");
                    }
                } else {
                    // Here goes the implementation of refresh the configuration
                }
            }
        }
    }
}
