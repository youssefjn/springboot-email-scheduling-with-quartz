package com.example.quartz.job;

import java.nio.charset.StandardCharsets;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
@Component
public class EmailJob extends QuartzJobBean{
 private static final Logger LOG = LoggerFactory.getLogger(EmailJob.class);
 @Autowired
 private JavaMailSender javaMailSender;
 @Autowired
 private MailProperties mailProperties;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LOG.info("executing job with key {}",context.getJobDetail().getKey());
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");
        String recipientEmail = jobDataMap.getString("email");
        sendMail(mailProperties.getUsername(),recipientEmail,subject,body);
    }
    private void sendMail(String fromEmail, String toEmail, String subject, String body) {
        try {
            LOG.info("Sending Email to {}", toEmail);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper=new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
            messageHelper.setSubject(subject);
            messageHelper.setText(body,true);
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(toEmail);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            LOG.error("Failed to send Email to {}", toEmail);
        }
    }
    
}
