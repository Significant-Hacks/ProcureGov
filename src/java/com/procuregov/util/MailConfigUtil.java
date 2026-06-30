package com.procuregov.util;

import javax.naming.InitialContext;
import javax.mail.Session;

/**
 * Mail configuration utility using JNDI resources.
 * No properties files needed - uses Java EE standard approach.
 */
public class MailConfigUtil {
    
    /**
     * Gets mail session from JNDI - no external configuration files needed
     * @return Mail session configured in context.xml
     */
    public static Session getMailSession() throws Exception {
        InitialContext ctx = new InitialContext();
        return (Session) ctx.lookup("java:comp/env/mail/ProcureGovMail");
    }
}
