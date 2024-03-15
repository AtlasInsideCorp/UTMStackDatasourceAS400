package com.extractor.as400.enums;

import java.util.Arrays;
import java.util.Optional;

public enum InstallationOptions {
    RUN("RUN"),
    INSTALL("INSTALL"),
    UNINSTALL("UNINSTALL"),
    UNRECOGNIZED_OPTION("UNRECOGNIZED_OPTION");

    private String eValue;

    InstallationOptions(String eValue) {
        this.eValue = eValue;
    }

    private String get() {
        return this.eValue;
    }

    public static InstallationOptions getByValue (String value) {
        try {
            Optional<InstallationOptions> val = Arrays
                    .stream(InstallationOptions.values())
                    .filter(repVal -> (repVal.get()).equals(value))
                    .findFirst();
            return val.get();
        } catch (java.util.NoSuchElementException ex) {
            return UNRECOGNIZED_OPTION;
        }
    }
}
