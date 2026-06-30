package com.procuregov.util;

import java.io.File;
import java.nio.file.Paths;

/**
 * File utility for handling uploads and downloads.
 * Uses relative paths only - no properties files or absolute paths.
 */
public class FileUtil {
    
    private static final String UPLOAD_DIR = "ProcureGov" + File.separator + "uploads";
    
    /**
     * Gets the upload directory path relative to user home.
     * No absolute paths hardcoded.
     */
    public static String getUploadDirectory() {
        return Paths.get(System.getProperty("user.home"), UPLOAD_DIR).toString();
    }
    
    /**
     * Creates upload directory if it doesn't exist.
     */
    public static void ensureUploadDirectoryExists() {
        File uploadDir = new File(getUploadDirectory());
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
    
    /**
     * Gets file path for a stored file.
     */
    public static String getFilePath(String fileName) {
        return Paths.get(getUploadDirectory(), fileName).toString();
    }
}