package com.extractor.as400.executors.impl;

import agent.CollectorOuterClass.RegisterRequest;
import agent.CollectorOuterClass.CollectorModule;
import agent.Common.AuthResponse;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.exceptions.InetUtilException;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.interfaces.IExecutor;
import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.util.InetUtil;
import com.extractor.as400.util.UsageHelp;
import com.utmstack.grpc.connection.GrpcConnection;
import com.utmstack.grpc.exception.CollectorServiceGrpcException;
import com.utmstack.grpc.exception.GrpcConnectionException;
import com.utmstack.grpc.jclient.config.interceptors.KeyStore;
import com.utmstack.grpc.service.CollectorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class InstallExecutor implements IExecutor {
    private static final String CLASSNAME = "InstallExecutor";
    private static final Logger logger = LogManager.getLogger(InstallExecutor.class);

    /**
     * @throws ExecutorAS400Exception
     */
    @Override
    public void execute() throws ExecutorAS400Exception {
        final String ctx = CLASSNAME + ".execute";

        // Check if the lock file exists before executing the installer
        if (!FileOperations.isLockFileCreated()) {
            try {
                // Begin gRPC connection
                String agentManagerHost = (String) UsageHelp.getParamsFromArgs().get("-host");
                String agentManagerPort = (String) UsageHelp.getParamsFromArgs().get("-port");
                String connectionKey = (String) UsageHelp.getParamsFromArgs().get("-connection-key");

                // Set the authentication needed for register a collector
                KeyStore.setConnectionKey(connectionKey);
                // Connectiong to gRPC server
                GrpcConnection con = new GrpcConnection();
                con.connectTo(agentManagerHost,
                        Integer.parseInt(agentManagerPort));

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
                FileOperations.createLockFile(response);
                logger.info(ctx + ": Collector registered successfully.");

            } catch (java.lang.NumberFormatException e) {
                throw new ExecutorAS400Exception(ctx + ": Invalid port value -> " + e.getMessage());
            } catch (GrpcConnectionException | CollectorServiceGrpcException | InetUtilException e) {
                throw new ExecutorAS400Exception(ctx + ": " + e.getMessage());
            } catch (IOException e) {
                throw new ExecutorAS400Exception(ctx + ": Error saving the collector installation information -> " + e.getMessage());
            }
        } else {
            throw new ExecutorAS400Exception(ctx + ": Other installation was detected, please uninstall the collector before install again.");
        }
    }
}
