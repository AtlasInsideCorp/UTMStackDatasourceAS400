package com.extractor.as400.util;

import com.extractor.as400.enums.ValidationTypeEnum;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to perform some validations
 * */
public class Validations {
    // Method used to validate integers from String values and return a controlled exception
    public static int validateNumber (String value, ValidationTypeEnum type) throws java.lang.NumberFormatException {
        try {
            return Integer.parseInt(value);
        } catch (java.lang.NumberFormatException e) {
            throw new NumberFormatException("Invalid "+ type.name() +" value -> " + e.getMessage());
        }
    }
}

