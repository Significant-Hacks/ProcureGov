package com.procuregov.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Email notification service using JavaMail API with SMTP authentication.
 * Reads SMTP configuration from servlet context init-params in web.xml.
 * Supports Gmail SMTP with STARTTLS and app-password authentication.
 * Sends notifications for: tender published, bid received, award decision.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private String smtpHost;
    private String smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private String mailFrom;
    private boolean configured;

    /**
     * Constructs EmailService with explicit SMTP credentials.
     * @param smtpHost SMTP server hostname (e.g. smtp.gmail.com)
     * @param smtpPort SMTP server port (e.g. 587)
     * @param smtpUser SMTP username (full email address)
     * @param smtpPassword SMTP password or app-specific password
     */
    public EmailService(String smtpHost, String smtpPort, String smtpUser, String smtpPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword;
        this.mailFrom = smtpUser;
        this.configured = (smtpUser != null && !smtpUser.isEmpty()
                        && smtpPassword != null && !smtpPassword.isEmpty());
        if (configured) {
            LOGGER.info("EmailService configured for " + smtpUser + "@" + smtpHost + ":" + smtpPort);
        } else {
            LOGGER.warning("EmailService NOT configured - SMTP user/password empty. Emails will not be sent.");
        }
    }

    /**
     * Convenience constructor using default Gmail SMTP settings.
     * Caller must still set user/password via the 4-arg constructor.
     */
    public EmailService() {
        this("smtp.gmail.com", "587", "", "");
    }

    /**
     * Sends an email notification using JavaMail API with SMTP authentication.
     * @param to recipient email address
     * @param subject email subject
     * @param body email body (plain text)
     * @return true if sent successfully
     */
    public boolean sendEmail(String to, String subject, String body) {
        if (!configured) {
            LOGGER.warning("EmailService not configured - skipping email to " + to);
            return false;
        }

        try {
            Session mailSession = createMailSession();

            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(mailFrom));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");

            Transport.send(message);
            LOGGER.info("Email sent to " + to + " with subject: " + subject);
            return true;

        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email to " + to, e);
            return false;
        }
    }

    /**
     * Creates a JavaMail Session configured for SMTP with STARTTLS authentication.
     * @return configured Session
     */
    private Session createMailSession() {
        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", smtpHost);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        return Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });
    }

    /**
     * Sends tender published notification to all registered suppliers.
     * @param supplierEmails list of supplier email addresses
     * @param tenderRef tender reference number
     * @param tenderTitle tender title
     * @param deadline submission deadline
     */
    public void notifyTenderPublished(java.util.List<String> supplierEmails,
                                       String tenderRef, String tenderTitle, String deadline) {
        String subject = "New Tender Published: " + tenderRef;
        String body = "Dear Supplier,\n\n"
                + "A new tender has been published on the ProcureGov portal:\n\n"
                + "Reference: " + tenderRef + "\n"
                + "Title: " + tenderTitle + "\n"
                + "Submission Deadline: " + deadline + "\n\n"
                + "Please log in to the ProcureGov portal to view details and submit your bid.\n\n"
                + "Regards,\nProcureGov System";

        for (String email : supplierEmails) {
            sendEmail(email, subject, body);
        }
    }

    /**
     * Sends bid received confirmation to a supplier.
     * @param supplierEmail supplier email address
     * @param tenderRef tender reference number
     * @param tenderTitle tender title
     */
    public void notifyBidReceived(String supplierEmail, String tenderRef, String tenderTitle) {
        String subject = "Bid Submitted Successfully: " + tenderRef;
        String body = "Dear Supplier,\n\n"
                + "Your bid for the following tender has been received:\n\n"
                + "Reference: " + tenderRef + "\n"
                + "Title: " + tenderTitle + "\n\n"
                + "You will be notified when the evaluation is complete.\n\n"
                + "Regards,\nProcureGov System";

        sendEmail(supplierEmail, subject, body);
    }

    /**
     * Sends award decision notification to a supplier with outcome (Won/Not Won).
     * Per Module 6: notification must include tender reference, outcome, and link to award notice.
     * @param supplierEmail supplier email address
     * @param tenderRef tender reference number
     * @param tenderTitle tender title
     * @param awardedValue the awarded contract value
     * @param isWinner whether this supplier won the contract
     * @param awardNoticeLink URL to the award notice page
     */
    public void notifyAwardDecision(String supplierEmail, String tenderRef,
                                     String tenderTitle, String awardedValue,
                                     boolean isWinner, String awardNoticeLink) {
        String subject;
        String body;

        if (isWinner) {
            subject = "Tender Awarded - " + tenderRef + " - WON";
            body = "Dear Supplier,\n\n"
                    + "PROCUREGOV - TENDER MANAGEMENT SYSTEM\n"
                    + "Ministry of Public Works, Kingdom of Lesotho\n\n"
                    + "TENDER AWARD NOTIFICATION\n\n"
                    + "Tender Reference: " + tenderRef + "\n"
                    + "Tender Title: " + tenderTitle + "\n"
                    + "Outcome: WON\n\n"
                    + "Congratulations! Your bid has been selected for award.\n\n"
                    + "Awarded Value: M " + awardedValue + "\n\n"
                    + "View Award Notice:\n" + awardNoticeLink + "\n\n"
                    + "Please log in to your supplier dashboard for full details.\n\n"
                    + "---\n"
                    + "This is an automated notification from ProcureGov.\n"
                    + "Ministry of Public Works, Kingdom of Lesotho.";
        } else {
            subject = "Tender Awarded - " + tenderRef + " - Not Won";
            body = "Dear Supplier,\n\n"
                    + "PROCUREGOV - TENDER MANAGEMENT SYSTEM\n"
                    + "Ministry of Public Works, Kingdom of Lesotho\n\n"
                    + "TENDER AWARD NOTIFICATION\n\n"
                    + "Tender Reference: " + tenderRef + "\n"
                    + "Tender Title: " + tenderTitle + "\n"
                    + "Outcome: NOT WON\n\n"
                    + "This tender has been awarded to another supplier.\n\n"
                    + "Awarded Value: M " + awardedValue + "\n\n"
                    + "View Award Notice:\n" + awardNoticeLink + "\n\n"
                    + "Thank you for your participation.\n\n"
                    + "---\n"
                    + "This is an automated notification from ProcureGov.\n"
                    + "Ministry of Public Works, Kingdom of Lesotho.";
        }

        sendEmail(supplierEmail, subject, body);
    }

    /**
     * Sends an HTML email using JavaMail API with SMTP authentication.
     * @param to recipient email address
     * @param subject email subject
     * @param htmlBody email body as HTML
     * @return true if sent successfully
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        if (!configured) {
            LOGGER.warning("EmailService not configured - skipping HTML email to " + to);
            return false;
        }

        try {
            Session mailSession = createMailSession();

            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(mailFrom));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);
            LOGGER.info("HTML email sent to " + to + " with subject: " + subject);
            return true;

        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send HTML email to " + to, e);
            return false;
        }
    }

    /**
     * Sends password reset email with a styled HTML body containing a 6-digit reset code.
     * @param to recipient email address
     * @param fullName user's display name
     * @param resetCode 6-digit reset code
     * @return true if sent successfully
     */
    public boolean sendPasswordResetEmail(String to, String fullName, String resetCode) {
        String subject = "ProcureGov - Password Reset Code";

        String htmlBody =
            "<!DOCTYPE html>"
            + "<html><head><style>"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }"
            + ".header { text-align: center; padding: 30px 0; border-bottom: 3px solid #1e3a5f; }"
            + ".logo { font-size: 24px; font-weight: bold; color: #1e3a5f; }"
            + ".content { padding: 40px 20px; }"
            + ".code-container { background: #f8f9fa; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; }"
            + ".code { font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #1e3a5f; font-family: 'Courier New', monospace; }"
            + ".label { font-size: 14px; color: #666; margin-bottom: 15px; }"
            + ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; border-top: 1px solid #e5e7eb; }"
            + ".warning { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0; }"
            + "</style></head>"
            + "<body>"
            + "<div class='header'><div class='logo'>ProcureGov</div>"
            + "<div style='font-size:12px;color:#666;'>Ministry of Public Works, Kingdom of Lesotho</div></div>"
            + "<div class='content'>"
            + "<h2>Password Reset Request</h2>"
            + "<p>Hello " + fullName + ",</p>"
            + "<p>We received a request to reset your ProcureGov account password. Your verification code is:</p>"
            + "<div class='code-container'>"
            + "<div class='label'>PASSWORD RESET CODE</div>"
            + "<div class='code'>" + resetCode + "</div>"
            + "</div>"
            + "<div class='warning'><strong>Time-Sensitive:</strong> This code expires in 1 hour.</div>"
            + "<p><strong>Security Tips:</strong></p>"
            + "<ul>"
            + "<li>Enter this code on the reset password page to set a new password</li>"
            + "<li>Never share this code with anyone</li>"
            + "<li>ProcureGov will never ask for your code via email</li>"
            + "<li>If you did not request this reset, you can safely ignore this email</li>"
            + "</ul>"
            + "</div>"
            + "<div class='footer'><p>This is an automated message from ProcureGov.</p>"
            + "<p>&copy; Ministry of Public Works, Kingdom of Lesotho</p></div>"
            + "</body></html>";

        return sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Sends email verification code during supplier registration.
     * @param to recipient email address
     * @param code 6-digit verification code
     * @return true if sent successfully
     */
    public boolean sendEmailVerificationCode(String to, String code) {
        String subject = "ProcureGov - Email Verification Code";

        String htmlBody =
            "<!DOCTYPE html>"
            + "<html><head><style>"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }"
            + ".header { text-align: center; padding: 30px 0; border-bottom: 3px solid #1e3a5f; }"
            + ".logo { font-size: 24px; font-weight: bold; color: #1e3a5f; }"
            + ".content { padding: 40px 20px; }"
            + ".code-container { background: #f8f9fa; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; }"
            + ".code { font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #1e3a5f; font-family: 'Courier New', monospace; }"
            + ".label { font-size: 14px; color: #666; margin-bottom: 15px; }"
            + ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; border-top: 1px solid #e5e7eb; }"
            + ".warning { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0; }"
            + "</style></head>"
            + "<body>"
            + "<div class='header'><div class='logo'>ProcureGov</div>"
            + "<div style='font-size:12px;color:#666;'>Ministry of Public Works, Kingdom of Lesotho</div></div>"
            + "<div class='content'>"
            + "<h2>Email Verification</h2>"
            + "<p>Thank you for registering on ProcureGov. Please verify your email address by entering the following code on the registration page:</p>"
            + "<div class='code-container'>"
            + "<div class='label'>VERIFICATION CODE</div>"
            + "<div class='code'>" + code + "</div>"
            + "</div>"
            + "<div class='warning'><strong>Time-Sensitive:</strong> This code expires in 10 minutes.</div>"
            + "<p>If you did not create an account, you can safely ignore this email.</p>"
            + "</div>"
            + "<div class='footer'><p>This is an automated message from ProcureGov.</p>"
            + "<p>&copy; Ministry of Public Works, Kingdom of Lesotho</p></div>"
            + "</body></html>";

        return sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Notifies evaluators that a tender has entered evaluation (first score submitted).
     * Sent to all assigned evaluators so they know evaluation has begun.
     * @param evaluatorEmail email of the evaluator to notify
     * @param tenderRef tender reference number
     * @param tenderTitle tender title
     * @param firstEvaluatorName name of the evaluator who started the evaluation
     */
    public void notifyEvaluationStarted(String evaluatorEmail, String tenderRef, String tenderTitle, String firstEvaluatorName) {
        String subject = "Evaluation Started: " + tenderRef;
        String body = "Dear Evaluator,\n\n"
                + "PROCUREGOV - TENDER MANAGEMENT SYSTEM\n"
                + "Ministry of Public Works, Kingdom of Lesotho\n\n"
                + "EVALUATION HAS BEGUN\n\n"
                + "Tender Reference: " + tenderRef + "\n"
                + "Tender Title: " + tenderTitle + "\n\n"
                + firstEvaluatorName + " has submitted the first evaluation score for this tender.\n"
                + "The tender is now under active evaluation by the committee.\n\n"
                + "Please log in to the ProcureGov portal to complete your evaluation if you have not already done so.\n\n"
                + "---\n"
                + "This is an automated notification from ProcureGov.\n"
                + "Ministry of Public Works, Kingdom of Lesotho.";

        sendEmail(evaluatorEmail, subject, body);
    }

    /**
     * Reminds an evaluator who has not yet completed their evaluation and is not online.
     * Sent when other evaluators have completed their scores but this evaluator has not.
     * @param evaluatorEmail email of the evaluator to remind
     * @param tenderRef tender reference number
     * @param tenderTitle tender title
     * @param completedCount number of evaluators who have completed
     * @param totalEvaluators total number of assigned evaluators
     */
    public void notifyEvaluationReminder(String evaluatorEmail, String tenderRef, String tenderTitle,
                                          int completedCount, int totalEvaluators) {
        String subject = "Evaluation Reminder: " + tenderRef + " - Action Required";
        String body = "Dear Evaluator,\n\n"
                + "PROCUREGOV - TENDER MANAGEMENT SYSTEM\n"
                + "Ministry of Public Works, Kingdom of Lesotho\n\n"
                + "EVALUATION REMINDER - ACTION REQUIRED\n\n"
                + "Tender Reference: " + tenderRef + "\n"
                + "Tender Title: " + tenderTitle + "\n\n"
                + "You have not yet completed your evaluation for this tender.\n"
                + completedCount + " of " + totalEvaluators + " assigned evaluators have completed their evaluations.\n\n"
                + "Your evaluation is needed before the tender can proceed to the award stage.\n"
                + "Please log in to the ProcureGov portal as soon as possible to complete your scoring.\n\n"
                + "---\n"
                + "This is an automated reminder from ProcureGov.\n"
                + "Ministry of Public Works, Kingdom of Lesotho.";

        sendEmail(evaluatorEmail, subject, body);
    }

    /**
     * Notifies procurement officers that all evaluations are complete for a tender.
     * Sent when the tender transitions to Evaluated status and officers are not online.
     * @param officerEmail email of the officer to notify
     * @param tenderRef tender reference number
     * @param tenderTitle tender title
     */
    public void notifyEvaluationComplete(String officerEmail, String tenderRef, String tenderTitle) {
        String subject = "Evaluation Complete: " + tenderRef + " - Ready for Award";
        String body = "Dear Procurement Officer,\n\n"
                + "PROCUREGOV - TENDER MANAGEMENT SYSTEM\n"
                + "Ministry of Public Works, Kingdom of Lesotho\n\n"
                + "ALL EVALUATIONS COMPLETE - AWARD DECISION REQUIRED\n\n"
                + "Tender Reference: " + tenderRef + "\n"
                + "Tender Title: " + tenderTitle + "\n\n"
                + "All assigned evaluators have completed their evaluations for this tender.\n"
                + "The tender is now ready for your award decision.\n\n"
                + "Please log in to the ProcureGov portal to review the evaluation results and award the contract.\n\n"
                + "---\n"
                + "This is an automated notification from ProcureGov.\n"
                + "Ministry of Public Works, Kingdom of Lesotho.";

        sendEmail(officerEmail, subject, body);
    }

    /**
     * Returns whether this service is properly configured with SMTP credentials.
     * @return true if SMTP user and password are set
     */
    public boolean isConfigured() {
        return configured;
    }
}
