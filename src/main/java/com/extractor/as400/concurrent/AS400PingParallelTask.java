package com.extractor.as400.concurrent;

import agent.Common.AuthResponse;
import agent.Common.ConnectorType;
import agent.Ping.PingRequest;
import com.extractor.as400.config.AS400ExtractorConstants;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.enums.ValidationTypeEnum;
import com.extractor.as400.util.Validations;
import com.utmstack.grpc.connection.GrpcConnection;
import com.utmstack.grpc.exception.GrpcConnectionException;
import com.utmstack.grpc.exception.PingException;
import com.utmstack.grpc.jclient.config.Constants;
import com.utmstack.grpc.jclient.config.interceptors.impl.GrpcConnectionKeyInterceptor;
import com.utmstack.grpc.jclient.config.interceptors.impl.GrpcEmptyAuthInterceptor;
import com.utmstack.grpc.service.PingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to perform continuous ping requests to the collector manager,
 * to check if the collector is alive
 */
public class AS400PingParallelTask implements Runnable {
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
    private static final String CLASSNAME = "AS400PingParallelTask";
    private static final Logger logger = LogManager.getLogger(AS400PingParallelTask.class);

    // Used to set the TimeUnit to execute the ping request
    private TimeUnit pingTimeUnit = TimeUnit.SECONDS;
    // Used to set the amount of 'pingTimeUnit' value to execute the ping request
    private int pingTimeAmount = 10;

    // Used only for initialization
    public AS400PingParallelTask () {}

    // Setter methods to modify default values for local variables
    public AS400PingParallelTask withPingTimeUnit(TimeUnit pingTimeUnit) {
        this.pingTimeUnit = pingTimeUnit;
        return this;
    }

    public AS400PingParallelTask withPingTimeAmount(int pingTimeAmount) {
        this.pingTimeAmount = pingTimeAmount;
        return this;
    }
    public AS400PingParallelTask build() {
        return this;
    }

    @Override
    public void run() {
        final String ctx = CLASSNAME + ".run";
            try {
                // Read the collector information
                Map<String, String> info = FileOperations.getCollectorInfo();

                // Validating port and collector id
                int collectorMport = Validations.validateNumber(info.get(AS400ExtractorConstants.COLLECTOR_MANAGER_PORT), ValidationTypeEnum.PORT);
                int collectorId = Validations.validateNumber(info.get(Constants.COLLECTOR_ID_HEADER),ValidationTypeEnum.ID);

                // Connectiong to gRPC server
                GrpcConnection con = new GrpcConnection();
                    con.createChannel(info.get(AS400ExtractorConstants.COLLECTOR_MANAGER_HOST), collectorMport,
                            new GrpcEmptyAuthInterceptor());

                    // Creating ping requests of the current collector
                    PingRequest pingRequest = PingRequest.newBuilder()
                            .setAuth(AuthResponse.newBuilder()
                                    .setId(collectorId)
                                    .setKey(info.get(Constants.COLLECTOR_KEY_HEADER)).build())
                            .setType(ConnectorType.COLLECTOR)
                            .build();

                    // Creating the ping service and perform continuous ping
                    PingService servPing = new PingService(con);
                    servPing.ping(pingRequest, pingTimeUnit,pingTimeAmount);

            } catch (NumberFormatException | GrpcConnectionException | PingException e) {
                logger.error(ctx + ": " + e.getMessage());
            } catch (IOException e) {
                logger.error(ctx + ": Error reading the collector information -> " + e.getMessage());
            }
    }
}
