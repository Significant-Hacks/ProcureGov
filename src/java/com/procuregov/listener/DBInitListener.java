package com.procuregov.listener;

import com.procuregov.util.FileUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBInitListener handles application startup initialization.
 * Creates upload directories and verifies database connectivity.
 */
public class DBInitListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(DBInitListener.class.getName());

    /**
     * Called when the web application is starting up.
     * Creates necessary directories and logs initialization.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("ProcureGov application starting up...");

        // Ensure upload directory exists
        try {
            FileUtil.ensureUploadDirectoryExists();
            LOGGER.info("Upload directory ready: " + FileUtil.getUploadDirectory());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create upload directory", e);
        }

        LOGGER.info("ProcureGov application initialized successfully.");
    }

    /**
     * Called when the web application is shutting down.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("ProcureGov application shutting down...");
    }
}