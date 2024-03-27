package com.extractor.as400.util;


import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.ForwarderEnum;
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
        AtomicBoolean verify = new AtomicBoolean(true);
        StringBuilder paramErrors = new StringBuilder();

        try {
            int allowedParamsCount = AllowedParamsEnum.getAllowedParams().length;
            if (args.length != allowedParamsCount) {
                logger.error(ctx + ": The number of params is not correct: Expected -> " + allowedParamsCount + ", Found -> " + args.length);
                return false;
            }
            for (String arg : args) {
                int splitPosition = arg.indexOf('=');
                if (splitPosition == -1) {
                    paramErrors.append("\nThe argument -> ").append(arg).append(" is not well formed, must be: -param=value, please check.");
                    verify.set(false);
                } else {
                    // Find the param in the argument, the param is at the first position in key=value pair
                    String findParam = arg.substring(0, splitPosition);
                    String valueParam = arg.substring(splitPosition + 1);

                    // Begin parameters checks
                    AllowedParamsEnum allowedParam = AllowedParamsEnum.getByValue(findParam);
                    if (allowedParam.equals(AllowedParamsEnum.UNRECOGNIZED_PARAM)) {
                        paramErrors.append("\nUnrecognized parameter -> ").append(findParam).append(", please check.");
                        verify.set(false);
                    } else {
                        // If it has a value, then save it
                        if (StringUtil.hasText(valueParam)) {
                            params.put(findParam, valueParam);
                        } else {
                            paramErrors.append("\nThe parameter -> ").append(findParam).append(", don't have value, please check.");
                            verify.set(false);
                        }
                    }
                }
            }
            // Finally, verify that all the params are present
            Arrays.stream(AllowedParamsEnum.values()).forEach(p -> {
                if (p != AllowedParamsEnum.UNRECOGNIZED_PARAM && !params.containsKey(p.get())) {
                    paramErrors.append("\nThe parameter -> ").append(p.get()).append(", is missing, please check.");
                    verify.set(false);
                }
            });
        } catch (Exception e) {
            logger.error(ctx + ": " + paramErrors + ". " + e.getMessage());
            return false;
        }
        if (StringUtil.hasText(paramErrors.toString())) {
            logger.error(ctx + ": " + paramErrors);
        }
        return verify.get();
    }

    public static Map<String, Object> getParamsFromArgs() {
        return params;
    }
}
