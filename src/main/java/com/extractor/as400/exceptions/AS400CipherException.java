package com.extractor.as400.exceptions;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to catch exceptions related to encrypt and decrypt operations
 * */
public class AS400CipherException extends Exception {
    public AS400CipherException(String message) {
        super(message);
    }
}
