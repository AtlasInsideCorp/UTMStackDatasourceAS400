package com.extractor.as400.connector.factory;

import com.extractor.as400.connector.connectors.AS400Connector;
import com.extractor.as400.connector.connectors.SyslogConnector;
import com.extractor.as400.enums.ConnectorEnum;
import com.extractor.as400.interfaces.IConnector;

public class ConnectorFactory {
    public ConnectorFactory() {
    }

    public IConnector getConnectorByType (String TYPE){
        if (TYPE.compareTo(ConnectorEnum.AS400_V1.get())==0) {
            return new AS400Connector();
        } else if (TYPE.compareTo(ConnectorEnum.SYSLOG_V1.get())==0) {
            return new SyslogConnector();
        }
    return null;
    }
}
