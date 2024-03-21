package com.extractor.as400.executors.impl;

import agent.CollectorOuterClass.RegisterRequest;
import agent.CollectorOuterClass.CollectorModule;
import agent.Common.AuthResponse;
import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.ValidationTypeEnum;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.exceptions.InetUtilException;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.interfaces.IExecutor;
import com.extractor.as400.models.CollectorFileConfiguration;
import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.util.InetUtil;
import com.extractor.as400.util.UsageHelp;
import com.extractor.as400.util.Validations;
import com.utmstack.grpc.connection.GrpcConnection;
import com.utmstack.grpc.exception.CollectorServiceGrpcException;
import com.utmstack.grpc.exception.GrpcConnectionException;
import com.utmstack.grpc.jclient.config.interceptors.KeyStore;
import com.utmstack.grpc.service.CollectorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to perform the initial actions when installing the collector
 * Register the collector and save configuration for latter use
 * */
public class InstallExecutor implements IExecutor {
    private static final String CLASSNAME = "InstallExecutor";
    private static final Logger logger = LogManager.getLogger(InstallExecutor.class);

    /**
     * Method that execute the basic installation actions
     * @throws ExecutorAS400Exception if the actions of register the collector or saving the collector info
     * can't be executed; or the installation was already performed.
     */
    @Override
    public void execute() throws ExecutorAS400Exception {
        final String ctx = CLASSNAME + ".execute";

        // Check if the lock file exists before executing the installer
        if (!FileOperations.isLockFileCreated()) {
            try {
                // Begin gRPC connection
                String agentManagerHost = (String) UsageHelp.getParamsFromArgs().get(AllowedParamsEnum.PARAM_HOST.get());
                String agentManagerPort = (String) UsageHelp.getParamsFromArgs().get(AllowedParamsEnum.PARAM_PORT.get());
                String connectionKey = (String) UsageHelp.getParamsFromArgs().get(AllowedParamsEnum.PARAM_CONNECTION_KEY.get());

                // Set the authentication needed for register a collector
                KeyStore.setConnectionKey(connectionKey);
                // Connectiong to gRPC server
                GrpcConnection con = new GrpcConnection();
                con.connectTo(agentManagerHost,
                        Validations.validateNumber(agentManagerPort, ValidationTypeEnum.PORT));

                // Register request
                InetUtil.searchForLocalIP();
                RegisterRequest req = RegisterRequest.newBuilder()
                        .setIp(InetUtil.getIp())
                        .setHostname(InetUtil.getHostname())
                        .setVersion(ConfigVerification.API_VERSION_SHORT)
                        .setCollector(CollectorModule.AS_400)
                        .build();

                // Instantiating the collector service
                CollectorService serv = new CollectorService(con);
                AuthResponse response = serv.registerCollector(req);

                // Saving collector info in the lock file
                CollectorFileConfiguration config = new CollectorFileConfiguration(response, agentManagerHost, Integer.parseInt(agentManagerPort));
                FileOperations.createLockFile(config);
                logger.info(ctx + ": Collector registered successfully.");

            } catch (NumberFormatException | GrpcConnectionException | CollectorServiceGrpcException |
                     InetUtilException e) {
                throw new ExecutorAS400Exception(ctx + ": " + e.getMessage());
            } catch (IOException e) {
                throw new ExecutorAS400Exception(ctx + ": Error saving the collector installation information -> " + e.getMessage());
            }
        } else {
            throw new ExecutorAS400Exception(ctx + ": Other installation was detected, please uninstall the collector before install again.");
        }
    }
}
