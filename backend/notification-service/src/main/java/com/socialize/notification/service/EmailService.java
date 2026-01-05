package com.socialize.notification.service;

import com.socialize.notification.dto.NotificationEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.UnsupportedEncodingException;

import java.util.Map;

/**
 * Email Service - Sends email notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.from-name}")
    private String fromName;

    @Value("${notification.email.enabled}")
    private boolean emailEnabled;

    /**
     * Send request received email to host
     */
    @Async
    public void sendRequestReceivedEmail(NotificationEvent event) {
        if (!emailEnabled) {
            log.debug("Email notifications disabled");
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("title", event.getTitle());
            context.setVariable("message", event.getMessage());
            context.setVariable("recipientName", event.getRecipientName());
            
            if (event.getData() != null) {
                context.setVariables(event.getData());
            }

            String htmlContent = templateEngine.process("request-received", context);
            
            sendHtmlEmail(
                event.getRecipientEmail(),
                "New Join Request - socialize",
                htmlContent
            );
            
            log.info("Request received email sent to: {}", event.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send request received email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send request approved email
     */
    @Async
    public void sendRequestApprovedEmail(NotificationEvent event) {
        if (!emailEnabled) return;

        try {
            Context context = new Context();
            context.setVariable("title", event.getTitle());
            context.setVariable("message", event.getMessage());
            context.setVariable("recipientName", event.getRecipientName());
            
            if (event.getData() != null) {
                context.setVariables(event.getData());
            }

            String htmlContent = templateEngine.process("request-approved", context);
            
            sendHtmlEmail(
                event.getRecipientEmail(),
                "Request Approved - socialize",
                htmlContent
            );
            
            log.info("Request approved email sent to: {}", event.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send request approved email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send event update email
     */
    @Async
    public void sendEventUpdateEmail(NotificationEvent event) {
        if (!emailEnabled) return;

        try {
            Context context = new Context();
            context.setVariable("title", event.getTitle());
            context.setVariable("message", event.getMessage());
            context.setVariable("recipientName", event.getRecipientName());
            
            if (event.getData() != null) {
                context.setVariables(event.getData());
            }

            String htmlContent = templateEngine.process("event-update", context);
            
            sendHtmlEmail(
                event.getRecipientEmail(),
                "Event Update - socialize",
                htmlContent
            );
            
            log.info("Event update email sent to: {}", event.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send event update email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send generic HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
        
            mailSender.send(message);
        }catch(MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!emailEnabled) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email: {}", e.getMessage(), e);
        }
    }
}