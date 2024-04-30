package com.extractor.as400.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum InstallationOptionsEnum {
    RUN("RUN"),
    INSTALL("INSTALL"),
    UNINSTALL("UNINSTALL"),
    UNRECOGNIZED_OPTION("UNRECOGNIZED_OPTION");

    private String eValue;

    InstallationOptionsEnum(String eValue) {
        this.eValue = eValue;
    }

    public String get() {
        return this.eValue;
    }

    public static InstallationOptionsEnum getByValue (String value) {
        try {
            Optional<InstallationOptionsEnum> val = Arrays
                    .stream(InstallationOptionsEnum.values())
                    .filter(repVal -> (repVal.get()).equals(value))
                    .findFirst();
            return val.get();
        } catch (java.util.NoSuchElementException ex) {
            return UNRECOGNIZED_OPTION;
        }
    }
    /**
     * Method to get all the InstallationOptionsEnum as object array, excluding UNRECOGNIZED_PARAM
     * */
    public static Object [] getAllowedOptions () {
        return Arrays.stream(InstallationOptionsEnum.values()).filter(f-> !f.equals(InstallationOptionsEnum.UNRECOGNIZED_OPTION)).toArray();
    }

    public static List<AllowedParamsEnum> getOptionalParamsByOption (InstallationOptionsEnum option) {
        switch (option) {
            case RUN:
                return List.of(AllowedParamsEnum.PARAM_CONNECTION_KEY,AllowedParamsEnum.PARAM_HOST, AllowedParamsEnum.PARAM_PORT);
            default:
                return new ArrayList<>();
        }
    }
}
