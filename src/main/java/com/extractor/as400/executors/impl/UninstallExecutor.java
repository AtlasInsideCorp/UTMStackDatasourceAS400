package com.extractor.as400.executors.impl;

import agent.CollectorOuterClass.CollectorDelete;
import agent.Common.AuthResponse;
import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.ValidationTypeEnum;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.exceptions.InetUtilException;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.interfaces.IExecutor;
import com.extractor.as400.util.InetUtil;
import com.extractor.as400.util.UsageHelp;
import com.extractor.as400.util.Validations;
import com.utmstack.grpc.connection.GrpcConnection;
import com.utmstack.grpc.exception.CollectorServiceGrpcException;
import com.utmstack.grpc.exception.GrpcConnectionException;
import com.utmstack.grpc.jclient.config.Constants;
import com.utmstack.grpc.service.CollectorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to perform the initial actions when uninstalling the collector
 * Unregister the collector and remove local configuration from disk
 */
public class UninstallExecutor implements IExecutor {
    private static final String CLASSNAME = "UninstallExecutor";
    private static final Logger logger = LogManager.getLogger(UninstallExecutor.class);

    /**
     * Method that execute the basic installation actions
     *
     * @throws ExecutorAS400Exception if the actions of register the collector or saving the collector info
     *                                can't be executed; or the installation was already performed.
     */
    @Override
    public void execute() throws ExecutorAS400Exception {
        final String ctx = CLASSNAME + ".execute";

        // Check if the lock file exists before executing the uninstaller
        if (FileOperations.isLockFileCreated()) {
            try {
                // Begin gRPC connection
                String collectorManagerHost = (String) UsageHelp.getParamsMap().get(AllowedParamsEnum.PARAM_HOST.get());
                String collectorManagerPort = (String) UsageHelp.getParamsMap().get(AllowedParamsEnum.PARAM_PORT.get());

                // Connectiong to gRPC server
                GrpcConnection con = new GrpcConnection();
                con.createChannel(collectorManagerHost,
                        Validations.validateNumber(collectorManagerPort, ValidationTypeEnum.PORT), null);

                // Delete request
                InetUtil.searchForLocalIP();
                Map<String, String> info = FileOperations.getCollectorInfo();
                CollectorDelete req = CollectorDelete.newBuilder()
                        .setDeletedBy(InetUtil.getHostname())
                        .build();

                // Instantiating the collector service
                CollectorService serv = new CollectorService(con);
                int id = Validations.validateNumber(info.get(Constants.COLLECTOR_ID_HEADER), ValidationTypeEnum.ID);
                // Calling gRPC delete function
                serv.deleteCollector(req, AuthResponse.newBuilder().setId(id).setKey(info.get(Constants.COLLECTOR_KEY_HEADER)).build());

                logger.info(ctx + ": Collector with key: " + info.get(Constants.COLLECTOR_KEY_HEADER) + ", was removed successfully.");

                // Removing collector info
                logger.info(ctx + ": Removing collector information and configurations from disk.");
                if (FileOperations.removeLockFile()) {
                    logger.info(ctx + ": Collector information was removed from disk.");
                } else {
                    logger.info(ctx + ": Collector information can't be removed from disk, maybe the file is locked by other program.");
                }

            } catch (NumberFormatException | GrpcConnectionException | CollectorServiceGrpcException | InetUtilException e) {
                throw new ExecutorAS400Exception(ctx + ": " + e.getMessage());
            } catch (IOException e) {
                throw new ExecutorAS400Exception(ctx + ": Error saving the collector installation information -> " + e.getMessage());
            }
        } else {
            throw new ExecutorAS400Exception(ctx + ": Installation wasn't detected, please install the collector before trying to uninstall.");
        }
    }
}
