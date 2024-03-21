package com.extractor.as400.enums;

import java.util.Arrays;
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

    private String get() {
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
}
