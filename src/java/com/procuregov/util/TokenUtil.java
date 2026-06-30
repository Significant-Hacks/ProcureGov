package com.procuregov.util;

import java.util.UUID;

/**
 * Utility class for generating secure tokens for password resets and account confirmation.
 */
public class TokenUtil {

    /**
     * Generates a unique token string using UUID.
     * @return a random UUID string
     */
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
