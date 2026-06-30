package com.procuregov.util;

import java.sql.Connection;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.logging.Logger;

public class DBConnectionUtil {
    
    private static final Logger LOGGER = Logger.getLogger(DBConnectionUtil.class.getName());

    public static Connection getConnection() throws Exception {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/ProcureGovDB");
        Connection conn = ds.getConnection();
        LOGGER.info("Database connection obtained via JNDI");
        return conn;
    }
}
