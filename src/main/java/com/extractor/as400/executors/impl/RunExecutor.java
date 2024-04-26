package com.extractor.as400.executors.impl;

import agent.Common;
import agent.Ping;
import com.extractor.as400.concurrent.AS400ConfigurationParallelTask;
import com.extractor.as400.concurrent.AS400PingParallelTask;
import com.extractor.as400.concurrent.AS400IngestParallelTask;
import com.extractor.as400.config.AS400ExtractorConstants;
import com.extractor.as400.config.InMemoryConfigurations;
import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.ForwarderEnum;
import com.extractor.as400.enums.ValidationTypeEnum;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.grpc.CallOneTimeCollectorConfig;
import com.extractor.as400.interfaces.IExecutor;
import com.extractor.as400.models.ServerState;
import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.util.ThreadsUtil;
import com.extractor.as400.util.UsageHelp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
            //Create a fixed executor to hold the ping requests
            ThreadPoolExecutor pingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            //Create a fixed executor to hold the configuration observer
            ThreadPoolExecutor configExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            //Launch ping thread
            try {
                pingExecutor.execute(new AS400PingParallelTask());
            } catch (Exception e) {
                logger.error(ctx + ": " + e.getMessage());
            }
            //Launch configuration thread
            try {
                configExecutor.execute(new AS400ConfigurationParallelTask());
            } catch (Exception e) {
                logger.error(ctx + ": " + e.getMessage());
            }

            //--------------------------------The concurrent ETL process is here-------------------------------------------
            boolean needConfigUpdate = true; // Request configurations in the first execution time
            while (true) {
                if (!needConfigUpdate) {
                    if (ConfigVerification.isEnvironmentOk()) {
                        //First we create fixed thread pool executor with (Servers count) threads, one per server
                        ThreadPoolExecutor etlExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(InMemoryConfigurations.getServerStateList().size() + 1);

                        Iterator<ServerState> serverStateIterator;
                        for (serverStateIterator = InMemoryConfigurations.getServerStateList().iterator(); serverStateIterator.hasNext(); ) {
                            try {
                                etlExecutor.execute(new AS400IngestParallelTask().withServerState(serverStateIterator.next())
                                        .withForwarder(ForwarderEnum.GRPC_LOG_AUTH_PROXY).build());
                            } catch (Exception e) {
                                logger.error(ctx + ": Error processing logs -> " + e.getMessage());
                            }
                        }

                        // Stopping executors
                        ThreadsUtil.stopThreadExecutor(etlExecutor);

                        // Finally, print summary of servers status
                        if (!InMemoryConfigurations.getServerStateList().isEmpty()) {
                            Iterator<ServerState> summaryIterator;
                            logger.info("***** SERVERS SUMMARY *****");
                            for (summaryIterator = InMemoryConfigurations.getServerStateList().iterator(); summaryIterator.hasNext(); ) {
                                ServerState tmp = summaryIterator.next();
                                logger.info("***** " + tmp.toString() + " *****");
                            }
                            logger.info("***** SERVERS SUMMARY (END) *****");
                        }
                    }
                    //
                    CountDownLatch waitLatch = new CountDownLatch(1);
                    try {
                        waitLatch.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        logger.error(ctx + ": System was interrupted while waiting for the next execution.");
                    }

                } else {
                    // Try to set the configuration and wait for a while
                    CountDownLatch waitLatch = new CountDownLatch(1);
                    if (CallOneTimeCollectorConfig.callOneTimeConfig()) {
                        needConfigUpdate = false;
                    }
                    try {
                        waitLatch.await(60, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        logger.error(ctx + ": System was interrupted while waiting after call for configuration.");
                        needConfigUpdate = false;
                    }
                }
            }
        }
    }
}
