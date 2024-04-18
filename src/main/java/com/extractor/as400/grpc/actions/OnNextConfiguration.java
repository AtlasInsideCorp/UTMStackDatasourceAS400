package com.extractor.as400.grpc.actions;

import agent.CollectorOuterClass.CollectorMessages;
import com.extractor.as400.config.InMemoryConfigurations;
import com.utmstack.grpc.service.iface.IExecuteActionOnNext;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to execute an action when StreamObserver call the onNext
 * This action normally will execute when Configurations arrive via gRPC
 * */
public class OnNextConfiguration implements IExecuteActionOnNext {


    public OnNextConfiguration() {}

    @Override
    public void executeOnNext(Object o) {
        if (o instanceof CollectorMessages) {
            if (((CollectorMessages) o).hasConfig() && !((CollectorMessages) o).getConfig().getGroupsList().isEmpty()) {
                InMemoryConfigurations.getFromGrpcConfigurations().clear();
                InMemoryConfigurations.getFromGrpcConfigurations().add(((CollectorMessages) o).getConfig());
            }
        }
    }
}
