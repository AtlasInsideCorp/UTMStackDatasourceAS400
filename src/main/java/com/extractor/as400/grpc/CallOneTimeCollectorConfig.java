package com.extractor.as400.grpc;

import agent.CollectorOuterClass.CollectorModule;
import agent.CollectorOuterClass.ConfigRequest;
import agent.CollectorOuterClass.CollectorConfig;
import agent.Common.AuthResponse;
import com.extractor.as400.config.AS400ExtractorConstants;
import com.extractor.as400.config.InMemoryConfigurations;
import com.extractor.as400.enums.ValidationTypeEnum;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.util.Validations;
import com.utmstack.grpc.connection.GrpcConnection;
import com.utmstack.grpc.exception.CollectorServiceGrpcException;
import com.utmstack.grpc.exception.GrpcConnectionException;
import com.utmstack.grpc.jclient.config.Constants;
import com.utmstack.grpc.jclient.config.interceptors.impl.GrpcEmptyAuthInterceptor;
import com.utmstack.grpc.service.CollectorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class CallOneTimeCollectorConfig {
    private static final String CLASSNAME = "CallOneTimeCollectorConfig";
    private static final Logger logger = LogManager.getLogger(CallOneTimeCollectorConfig.class);

    public static boolean callOneTimeConfig() {
        final String ctx = CLASSNAME + ".callOneTimeConfig";
        // Here goes the implementation of the request configuration
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

            CollectorService s = new CollectorService(con);
            ConfigRequest req = ConfigRequest.newBuilder().setModule(CollectorModule.AS_400).build();
            AuthResponse collector = AuthResponse.newBuilder().setKey(info.get(Constants.COLLECTOR_KEY_HEADER))
                    .setId(collectorId).build();
            CollectorConfig config = s.requestCollectorConfig(req, collector);
            if (config.getGroupsList().isEmpty()) {
                return false;
            } else {
                // Clear the list to insert the last config
                InMemoryConfigurations.getFromGrpcConfigurations().clear();
                InMemoryConfigurations.getFromGrpcConfigurations().add(config);
                // Updating in memory configuration list and prepare the list for the next configuration.
                InMemoryConfigurations.updateTemporaryConfigurationList();
                InMemoryConfigurations.updateConfigurationList();
                InMemoryConfigurations.getFromGrpcConfigurations().clear();
                return true;
            }

        } catch (NumberFormatException | GrpcConnectionException | CollectorServiceGrpcException e) {
            logger.error(ctx + ": " + e.getMessage());
            return false;
        } catch (IOException e) {
            logger.error(ctx + ": Error reading the collector information from the lock file -> " + e.getMessage());
            return false;
        }
    }
}
