package com.extractor.as400.exceptions;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to catch exceptions related to getting the IP Addr
 * of the localhost
 * */
public class InetUtilException extends Exception {
    public InetUtilException(String message) {
        super(message);
    }
}
