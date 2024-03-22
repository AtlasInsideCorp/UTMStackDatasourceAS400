package com.extractor.as400.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Freddy R. Laffita Almaguer
 * Enum used to manage the allowed forwarders (where to send logs through)
 * */
public enum ForwarderEnum {

    // Important: Don't insert more forwarders before this one
    UNRECOGNIZED_FORWARDER("UNRECOGNIZED_FORWARDER",0),
    //----------------------------------------------------------

    SYSLOG ("SYSLOG",1),
    GRPC_LOG_AUTH_PROXY("GRPC_LOG_AUTH_PROXY",100);

    private String fValue;
    private int fBatchSize;

    ForwarderEnum(String fValue, int fBatchSize) {
        this.fValue = fValue;
        this.fBatchSize = fBatchSize;
    }

    public String get() {
        return fValue;
    }

    public int getfBatchSize() {
        return fBatchSize;
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
        return Arrays.stream(ForwarderEnum.values()).filter(f-> !f.equals(ForwarderEnum.UNRECOGNIZED_FORWARDER)).toArray();
    }
}
