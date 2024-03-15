package com.extractor.as400.util;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is used to provide some execution parameters validations and usage help
 * */
public class UsageHelp {
    private static Map<String,Object> params = new LinkedHashMap<>();

    public static String usage() {
        return "Verify that the args passed to the program have this format: -option=RUN -host=localhost" +
                "\n*** Param -> Values ***" +
                "\n  -option: Can be INSTALL, UNINSTALL and RUN" +
                "\n  -host: Represents the host of the collector manager to connect to";
    }

    public static String duplicateErrorDesc() {
        return "\n *********** ERROR: Check your configuration" +
                ", some variables are not configured correctly ***********" +
                "\n * In /local_storage/Servers.json, can't be duplicated servers -> " +
                "(same hostname and tenant)";
    }

    public static String jsonErrorDesc() {
        return "\n *********** ERROR: Check your configuration" +
                ", some variables are not configured correctly ***********" +
                "\n * /local_storage/Servers.json file is required, can't be empty and must be a valid JSON * ";
    }

    public static boolean argsVerificationOk(String[] args) {
        try {
            if (args.length != 2) {
                return false;
            }
            for (String arg : args) {
                String [] tmpArg = arg.split("=");
                if (tmpArg.length != 2) {
                    return false;
                }
                if (!tmpArg[0].trim().equals("-option")
                        &&!tmpArg[0].trim().equals("-host")) {
                    return false;
                }
                params.put(tmpArg[0], tmpArg[1]);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public static Map<String,Object> getParamsFromArgs () {
        return params;
    }
}
