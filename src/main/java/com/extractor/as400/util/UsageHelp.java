package com.extractor.as400.util;


import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.InstallationOptionsEnum;
import com.utmstack.grpc.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to provide some execution parameters validations and usage help
 */
public class UsageHelp {
    private static final String CLASSNAME = "UsageHelp";
    private static final Logger logger = LogManager.getLogger(UsageHelp.class);
    private static Map<String, Object> params = new LinkedHashMap<>();

    public static String usage() {
        return "Verify that the args passed to the program have this format: -option=XXX -host=XXX -port=XXX -connection-key=XXX" +
                "\n*** Param -> Values ***" +
                "\n  -option: Can be one of " + Arrays.toString(InstallationOptionsEnum.getAllowedOptions()) +
                //"\n  -forward-to: Can be " + (ForwarderEnum.getAllowedForwarders().length > 1 ? "one of " : "only ") + Arrays.toString(ForwarderEnum.getAllowedForwarders()) +
                "\n  -host: Represents the host of the collector manager to connect to" +
                "\n  -port: Represents the port of the collector manager to connect to" + ", usually 50051 " +
                "\n  -connection-key: Represents the purchased key of your UTMStack instance" +
                "\n  Example: -option=INSTALL -forward-to=GRPC_LOG_AUTH_PROXY -host=localhost -port=50051 -connection-key=XXX";
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

    public static boolean argsVerification(String[] args) {

        final String ctx = CLASSNAME + ".argsVerification";
        StringBuilder paramErrors = new StringBuilder();

        try {
            paramErrors.append(processParamsFromArgs(args));
            int allowedParamsCount = AllowedParamsEnum.getAllowedParams().length;
            if (args.length > allowedParamsCount) {
                logger.error(ctx + ": The number of params exceed the max allowed: Max allowed -> {}, Found -> {}", allowedParamsCount, args.length);
                return false;
            }
            paramErrors.append(checkIfAllParamsArePresent());

        } catch (Exception e) {
            logger.error(ctx + ": " + paramErrors + ". " + e.getMessage());
            return false;
        }
        if (StringUtil.hasText(paramErrors.toString())) {
            logger.error(ctx + ": " + paramErrors);
        }
        return true;
    }

    public static Map<String, Object> getParamsMap() {
        return params;
    }

    /**
     * Method to process params from arguments and return the common errors.
     * Also, the method fills the map with final arguments and its values used
     * to define the execution parameters.
     * @param args the program arguments
     * @return StringBuilder with the common errors.
     * */
    private static StringBuilder processParamsFromArgs(String[] args) {
        StringBuilder paramErrors = new StringBuilder();

        for (String arg : args) {
            int splitPosition = arg.indexOf('=');
            if (splitPosition == -1) {
                paramErrors.append("\nThe argument -> ").append(arg).append(" is not well formed, must be: -param=value, please check.");
            } else {
                // Find the param in the argument, the param is at the first position in key=value pair
                String findParam = arg.substring(0, splitPosition);
                String valueParam = arg.substring(splitPosition + 1);

                // Begin parameters checks
                AllowedParamsEnum allowedParam = AllowedParamsEnum.getByValue(findParam);
                if (allowedParam.equals(AllowedParamsEnum.UNRECOGNIZED_PARAM)) {
                    paramErrors.append("\nUnrecognized parameter -> ").append(findParam).append(", please check.");
                } else {
                    // If it has a value, then save it
                    if (StringUtil.hasText(valueParam)) {
                        params.put(findParam, valueParam);
                    } else {
                        paramErrors.append("\nThe parameter -> ").append(findParam).append(", don't have value, please check.");
                    }
                }
            }
        }
        return paramErrors;
    }

    private static StringBuilder checkIfAllParamsArePresent() {
        StringBuilder paramErrors = new StringBuilder();
        // Finally, verify that all the params are present
        Arrays.stream(AllowedParamsEnum.values()).forEach(p -> {
            if (p != AllowedParamsEnum.UNRECOGNIZED_PARAM && !params.containsKey(p.get())) {
                // Ask for the optional params by option value, because for example, when 'RUN' -connection-key is not required
                if (params.containsKey(AllowedParamsEnum.PARAM_OPTION.get())) {
                    InstallationOptionsEnum option = InstallationOptionsEnum.getByValue(params.get(AllowedParamsEnum.PARAM_OPTION.get()).toString());
                    if (!InstallationOptionsEnum.getOptionalParamsByOption(option).contains(p)) {
                            paramErrors.append("\nThe parameter -> ").append(p.get()).append(", is missing, please check.");
                    }
                } else {
                    paramErrors.append("\n" + AllowedParamsEnum.PARAM_OPTION + " param is not present, because of that, even optional parameters will be marked as missing" +
                            " if it isn't specified. The parameter -> ").append(p.get()).append(", is missing, please check.");
                }
            }
        });
        return paramErrors;
    }
}
