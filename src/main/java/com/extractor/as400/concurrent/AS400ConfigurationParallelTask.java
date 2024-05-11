package com.extractor.as400.concurrent;

import agent.CollectorOuterClass.CollectorConfig;
import agent.CollectorOuterClass.CollectorMessages;
import agent.CollectorOuterClass.ConfigKnowledge;
import agent.Common.AuthResponse;
import com.extractor.as400.config.AS400ExtractorConstants;
import com.extractor.as400.config.InMemoryConfigurations;
import com.extractor.as400.enums.ValidationTypeEnum;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.grpc.actions.OnNextConfiguration;
import com.extractor.as400.util.Validations;
import com.utmstack.grpc.connection.GrpcConnection;
import com.utmstack.grpc.exception.CollectorServiceGrpcException;
import com.utmstack.grpc.exception.GrpcConnectionException;
import com.utmstack.grpc.jclient.config.Constants;
import com.utmstack.grpc.jclient.config.interceptors.impl.GrpcEmptyAuthInterceptor;
import com.utmstack.grpc.service.CollectorService;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to receive configuration changes from the collector manager,
 */
public class AS400ConfigurationParallelTask implements Runnable {
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    private static final String CLASSNAME = "AS400ConfigurationParallelTask";
    private static final Logger logger = LogManager.getLogger(AS400ConfigurationParallelTask.class);

    public AS400ConfigurationParallelTask() {
    }

    @Override
    public void run() {
        final String ctx = CLASSNAME + ".run";
        // Creating a latch to wait between stream creation calls
        final CountDownLatch waitLatch = new CountDownLatch(1);
        try {
            // Read the collector information
            Map<String, String> info = FileOperations.getCollectorInfo();

            // Validating port and collector id
            int collectorMport = Validations.validateNumber(info.get(AS400ExtractorConstants.COLLECTOR_MANAGER_PORT), ValidationTypeEnum.PORT);
            int collectorId = Validations.validateNumber(info.get(Constants.COLLECTOR_ID_HEADER), ValidationTypeEnum.ID);

            // Connectiong to gRPC server
            GrpcConnection con = new GrpcConnection();
            con.createChannel(info.get(AS400ExtractorConstants.COLLECTOR_MANAGER_HOST), collectorMport,
                    new GrpcEmptyAuthInterceptor());

            // Creating objects needed to get the stream
            AuthResponse collector = AuthResponse.newBuilder().setKey(info.get(Constants.COLLECTOR_KEY_HEADER))
                    .setId(collectorId).build();

            StreamObserver<CollectorMessages> collectorStreamObserver;
            CollectorService s = new CollectorService(con);

            // Creating the collector stream and wait for configurations
            final CountDownLatch finishLatch = new CountDownLatch(1);
            while (true) {
                try {
                    // Connecting to the stream
                        collectorStreamObserver = s.getCollectorStreamObserver(new OnNextConfiguration(),collector);

                    // Wait for server response
                    finishLatch.await(10, TimeUnit.SECONDS);
                    if (!InMemoryConfigurations.getFromGrpcConfigurations().isEmpty()) {
                        CollectorConfig config = InMemoryConfigurations.getFromGrpcConfigurations().get(0);
                        // Send confirmation back
                        ConfigKnowledge received = ConfigKnowledge.newBuilder()
                                .setAccepted("true").setRequestId(config.getRequestId()).build();

                        // Send confirmation, update in memory servers configuration and clear the list of CollectorConfig to avoid sending extra confirmations to server.
                        collectorStreamObserver.onNext(CollectorMessages.newBuilder().setResult(received).build());
                        InMemoryConfigurations.updateTemporaryConfigurationList();
                        InMemoryConfigurations.getFromGrpcConfigurations().clear();
                    }
                } catch (CollectorServiceGrpcException e) {
                    logger.error(ctx + ": " + e.getMessage());
                    waitLatch.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    String msg = ctx + ": Configuration stream was interrupted: " + e.getMessage();
                    logger.error(msg);
                }
            }


        } catch (NumberFormatException | GrpcConnectionException | InterruptedException e) {
            logger.error(ctx + ": " + e.getMessage());
        } catch (IOException e) {
            logger.error(ctx + ": Error reading the collector information from the lock file -> " + e.getMessage());
        }
    }
}
