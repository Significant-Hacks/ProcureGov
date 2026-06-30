package com.procuregov.util;

import java.security.MessageDigest;

/**
 * Utility class for password hashing using SHA-256.
 * Passwords must never be stored in plain text as per exam requirements.
 */
public class PasswordUtil {

    /**
     * Hashes a plain-text password using SHA-256.
     * @param password the plain-text password
     * @return the hex-encoded SHA-256 hash
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hash.
     * @param password the plain-text password to check
     * @param storedHash the stored SHA-256 hash
     * @return true if the password matches the hash
     */
    public static boolean verifyPassword(String password, String storedHash) {
        return hashPassword(password).equals(storedHash);
    }
}
