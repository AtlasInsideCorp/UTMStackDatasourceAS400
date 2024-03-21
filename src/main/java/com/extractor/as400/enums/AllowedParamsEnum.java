package com.extractor.as400.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Freddy R. Laffita Almaguer
 * Enum used to manage the allowed api parameters
 * */
public enum AllowedParamsEnum {
    // Important: Don't insert more params before this one
    UNRECOGNIZED_PARAM("UNRECOGNIZED_PARAM",0),
    //----------------------------------------------------------


    PARAM_OPTION("-option",2),
    PARAM_HOST("-host",2),
    PARAM_PORT("-port",2),
    PARAM_CONNECTION_KEY("-connection-key",2);


    // Represents the value of the enum
    private String eValue;

    // Represents the way to get the value
    // 0 = The param as is
    // 2 = Used for Key=Value pairs
    private int eSplittBy;

    AllowedParamsEnum(String eValue, int eSplittBy) {
        this.eValue = eValue;
        this.eSplittBy = eSplittBy;
    }

    public String get() {
        return this.eValue;
    }
    public int getSplittBy() {
        return this.eSplittBy;
    }

    /**
     * Method to get an instance of AllowedParamsEnum enum by its value
     * */
    public static AllowedParamsEnum getByValue (String value) {
        try {
            Optional<AllowedParamsEnum> val = Arrays
                    .stream(AllowedParamsEnum.values())
                    .filter(repVal -> (repVal.get()).equals(value))
                    .findFirst();
            return val.get();
        } catch (java.util.NoSuchElementException ex) {
            return UNRECOGNIZED_PARAM;
        }
    }
    /**
     * Method to get all the AllowedParamsEnum as object array, excluding UNRECOGNIZED_PARAM
     * */
    public static Object [] getAllowedParams () {
        return Arrays.stream(InstallationOptionsEnum.values()).filter(f-> !f.equals(InstallationOptionsEnum.UNRECOGNIZED_OPTION)).toArray();
    }
}
