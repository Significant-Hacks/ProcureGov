package com.procuregov.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration utility for reading config.properties
 * Only for environment variables - NOT NetBeans server paths
 */
public class ConfigUtil {
    
    private static final Properties props = new Properties();
    
    static {
        try (InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            // Default values if config.properties not found
            System.err.println("config.properties not found, using defaults");
        }
    }
    
    public static String getProperty(String key) {
        return props.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(props.getProperty(key, String.valueOf(defaultValue)));
    }
    
    // Database settings
    public static String getDbDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }
    
    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/ProcureGov?useSSL=false&allowPublicKeyRetrieval=true");
    }
    
    public static String getDbUsername() {
        return getProperty("db.username", "root");
    }
    
    public static String getDbPassword() {
        return getProperty("db.password", "");
    }
    
    // Email settings
    public static String getMailHost() {
        return getProperty("mail.smtp.host", "smtp.gmail.com");
    }
    
    public static int getMailPort() {
        return getIntProperty("mail.smtp.port", 587);
    }
    
    public static boolean getMailAuth() {
        return getBooleanProperty("mail.smtp.auth", true);
    }
    
    public static boolean getMailStartTLS() {
        return getBooleanProperty("mail.smtp.starttls.enable", true);
    }
    
    public static String getMailUser() {
        return getProperty("mail.smtp.user");
    }
    
    public static String getMailPassword() {
        return getProperty("mail.smtp.password");
    }
    
    // System settings
    public static int getMaxLoginAttempts() {
        return getIntProperty("login.max.attempts", 3);
    }
    
    public static int getAccountLockoutDuration() {
        return getIntProperty("account.lockout.duration", 30); // minutes
    }
    
    public static int getPasswordResetTokenExpiry() {
        return getIntProperty("password.reset.token.expiry", 30); // minutes
    }
    
    public static int getAccountConfirmationTokenExpiry() {
        return getIntProperty("account.confirmation.token.expiry", 1440); // 24 hours
    }
    
    public static String getAppName() {
        return getProperty("app.name", "ProcureGov");
    }
    
    public static String getAppVersion() {
        return getProperty("app.version", "1.0");
    }
    
    public static String getAdminEmail() {
        return getProperty("app.admin.email", "admin@procuregov.ls");
    }
}
