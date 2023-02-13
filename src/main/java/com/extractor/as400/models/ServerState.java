package com.extractor.as400.models;

public class ServerState {
    ServerDefAS400 serverDefAS400;
    String state;

    public ServerState(ServerDefAS400 serverDefAS400) {
        this.serverDefAS400 = serverDefAS400;
        this.state = "CREATED";
    }
    public ServerState(ServerDefAS400 serverDefAS400, String state) {
        this.serverDefAS400 = serverDefAS400;
        this.state = state;
    }

    public ServerDefAS400 getServerDefAS400() {
        return serverDefAS400;
    }

    public void setServerDefAS400(ServerDefAS400 serverDefAS400) {
        this.serverDefAS400 = serverDefAS400;
    }

    public String getStatus() {
        return state;
    }

    public void setStatus(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return serverDefAS400.toString()+" status ("+getStatus()+")";
    }
}
