package com.extractor.as400.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Freddy R. Laffita Almaguer
 * Enum used to manage the allowed forwarders (where to send logs through)
 * */
public enum ForwarderEnum {

    // Important: Don't insert more forwarders before this one
    UNRECOGNIZED_FORWARDER("UNRECOGNIZED_FORWARDER",0,true),
    //----------------------------------------------------------

    GRPC_LOG_AUTH_PROXY("GRPC_LOG_AUTH_PROXY",100,true),
    SYSLOG ("SYSLOG",1,false);

    private String fValue;
    private int fBatchSize;
    private boolean available;

    ForwarderEnum(String fValue, int fBatchSize, boolean available) {
        this.fValue = fValue;
        this.fBatchSize = fBatchSize;
        this.available = available;
    }

    public String get() {
        return fValue;
    }

    public int getfBatchSize() {
        return fBatchSize;
    }

    public boolean isAvailable() {
        return available;
    }

    public static ForwarderEnum getByValue (String value) {
        try {
            Optional<ForwarderEnum> val = Arrays
                    .stream(ForwarderEnum.values())
                    .filter(repVal -> (repVal.get()).equals(value))
                    .findFirst();
            return val.get();
        } catch (java.util.NoSuchElementException ex) {
            return UNRECOGNIZED_FORWARDER;
        }
    }
    /**
     * Method to get all the ForwarderEnum as object array, excluding UNRECOGNIZED_FORWARDER
     * */
    public static Object [] getAllowedForwarders () {
        return Arrays.stream(ForwarderEnum.values()).filter(f-> !f.equals(ForwarderEnum.UNRECOGNIZED_FORWARDER)
        && f.isAvailable()).toArray();
    }
}
