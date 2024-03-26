package com.extractor.as400.forwarders;

import com.extractor.as400.enums.ForwarderEnum;
import com.extractor.as400.forwarders.impl.LogAuthProxyForwarder;
import com.extractor.as400.forwarders.impl.SyslogForwarder;
import com.extractor.as400.interfaces.IForwarder;
import com.extractor.as400.models.ServerState;

public class ForwarderFactory {
    public ForwarderFactory() {
    }
    public IForwarder getForwarder(ForwarderEnum forwarder, ServerState as400Server){
        if(forwarder.equals(ForwarderEnum.SYSLOG)){
            return new SyslogForwarder().withServerState(as400Server).build();
        } else if(forwarder.equals(ForwarderEnum.GRPC_LOG_AUTH_PROXY)){
            return new LogAuthProxyForwarder().withServerState(as400Server).build();
        }
        return null;
    }
}
