package com.extractor.as400.executors;

import com.extractor.as400.enums.InstallationOptions;
import com.extractor.as400.executors.impl.InstallExecutor;
import com.extractor.as400.executors.impl.UninstallExecutor;
import com.extractor.as400.interfaces.IExecutor;

public class ExecutorFactory {
    public ExecutorFactory() {
    }
    public IExecutor getExecutor(InstallationOptions opts){
        if(opts.equals(InstallationOptions.INSTALL)){
            return new InstallExecutor();
        } else if (opts.equals(InstallationOptions.UNINSTALL)) {
            return new UninstallExecutor();
        } else if (opts.equals(InstallationOptions.RUN)) {
            return new UninstallExecutor();
        }
        return null;
    }
}
