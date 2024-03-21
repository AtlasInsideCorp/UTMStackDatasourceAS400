package com.extractor.as400.executors;

import com.extractor.as400.enums.InstallationOptionsEnum;
import com.extractor.as400.executors.impl.InstallExecutor;
import com.extractor.as400.executors.impl.RunExecutor;
import com.extractor.as400.executors.impl.UninstallExecutor;
import com.extractor.as400.interfaces.IExecutor;

public class ExecutorFactory {
    public ExecutorFactory() {
    }
    public IExecutor getExecutor(InstallationOptionsEnum opts){
        if(opts.equals(InstallationOptionsEnum.INSTALL)){
            return new InstallExecutor();
        } else if (opts.equals(InstallationOptionsEnum.UNINSTALL)) {
            return new UninstallExecutor();
        } else if (opts.equals(InstallationOptionsEnum.RUN)) {
            return new RunExecutor();
        }
        return null;
    }
}
