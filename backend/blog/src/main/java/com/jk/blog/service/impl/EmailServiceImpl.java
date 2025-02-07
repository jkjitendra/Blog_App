package com.jk.blog.service.impl;

import com.jk.blog.dto.MailBody;
import com.jk.blog.exception.EmailSendingException;
import com.jk.blog.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

  private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.from}")
  private String fromEmail;

  @Override
  public void sendEmail(MailBody mailBody) {
    try {

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(fromEmail);
      helper.setTo(mailBody.getTo());
      helper.setSubject(mailBody.getSubject());
      helper.setText(mailBody.getText(), true);

      mailSender.send(message);
      logger.info("Email sent to {}", mailBody.getTo());

    } catch (MailException | MessagingException e) {
      logger.error("Error sending email to {}: {}", mailBody.getTo(), e.getMessage());
      throw new EmailSendingException(mailBody.getTo(), e.getMessage());
    }
  }
}
