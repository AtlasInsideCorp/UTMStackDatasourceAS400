package com.extractor.as400.executors.impl;

import agent.CollectorOuterClass.CollectorModule;
import agent.CollectorOuterClass.CollectorResponse;
import agent.CollectorOuterClass.CollectorDelete;
import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.ValidationTypeEnum;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.interfaces.IExecutor;
import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.util.UsageHelp;
import com.extractor.as400.util.Validations;
import com.utmstack.grpc.connection.GrpcConnection;
import com.utmstack.grpc.exception.CollectorServiceGrpcException;
import com.utmstack.grpc.exception.GrpcConnectionException;
import com.utmstack.grpc.jclient.config.Constants;
import com.utmstack.grpc.jclient.config.interceptors.KeyStore;
import com.utmstack.grpc.service.CollectorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to perform the initial actions when uninstalling the collector
 * Unregister the collector and remove local configuration from disk
 * */
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
                String collectorManagerHost = (String) UsageHelp.getParamsFromArgs().get(AllowedParamsEnum.PARAM_HOST.get());
                String collectorManagerPort = (String) UsageHelp.getParamsFromArgs().get(AllowedParamsEnum.PARAM_PORT.get());
                String connectionKey = (String) UsageHelp.getParamsFromArgs().get(AllowedParamsEnum.PARAM_CONNECTION_KEY.get());

                // Set the authentication needed for unregister a collector
                KeyStore.setConnectionKey(connectionKey);
                // Connectiong to gRPC server
                GrpcConnection con = new GrpcConnection();
                    con.connectTo(collectorManagerHost,
                            Validations.validateNumber(collectorManagerPort, ValidationTypeEnum.PORT));

                // Delete request
                Map<String, String> info = FileOperations.getCollectorInfo();
                CollectorDelete req = CollectorDelete.newBuilder()
                        .setDeletedBy(CollectorModule.AS_400.name() + "-" + ConfigVerification.API_VERSION_SHORT)
                        .setCollectorKey(info.get(Constants.COLLECTOR_KEY_HEADER))
                        .build();

                // Instantiating the collector service
                CollectorService serv = new CollectorService(con);
                    int id = Validations.validateNumber(info.get(Constants.COLLECTOR_ID_HEADER),ValidationTypeEnum.ID);
                    CollectorResponse response = serv.deleteCollector(req, id);

                logger.info(ctx + ": Collector with key: " + info.get(Constants.COLLECTOR_KEY_HEADER) + ", was removed successfully.");

                // Removing collector info
                logger.info(ctx + ": Removing collector information and configurations from disk.");
                if(FileOperations.removeLockFile()) {
                    logger.info(ctx + ": Collector information was removed from disk.");
                } else {
                    logger.info(ctx + ": Collector information can't be removed from disk, maybe the file is locked by other program.");
                }
                if(FileOperations.removeConfigs()) {
                    logger.info(ctx + ": Collector configuration was removed from disk.");
                } else {
                    logger.info(ctx + ": Collector configuration can't be removed from disk, maybe the files are locked by other program.");
                }


            } catch (NumberFormatException | GrpcConnectionException | CollectorServiceGrpcException e) {
                throw new ExecutorAS400Exception(ctx + ": " + e.getMessage());
            } catch (IOException e) {
                throw new ExecutorAS400Exception(ctx + ": Error saving the collector installation information -> " + e.getMessage());
            }
        } else {
            throw new ExecutorAS400Exception(ctx + ": Installation wasn't detected, please install the collector before trying to uninstall.");
        }
    }
}
