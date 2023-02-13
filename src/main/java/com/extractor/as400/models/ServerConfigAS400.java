package com.extractor.as400.models;

import java.util.List;

public class ServerConfigAS400 {
    List<ServerDefAS400> serversAS400;

    public ServerConfigAS400(){}

    public ServerConfigAS400(List<ServerDefAS400> serversAS400) {
        this.serversAS400 = serversAS400;
    }

    public List<ServerDefAS400> getServersAS400() {
        return serversAS400;
    }

    public void setServersAS400(List<ServerDefAS400> serversAS400) {
        this.serversAS400 = serversAS400;
    }
}
