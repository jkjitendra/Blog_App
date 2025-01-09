package com.jk.blog.service.impl;

import com.jk.blog.dto.MailBody;
import com.jk.blog.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Override
  public void sendEmail(MailBody mailBody) {
    SimpleMailMessage message = new SimpleMailMessage();

    message.setTo(mailBody.getTo());
    message.setSubject(mailBody.getSubject());
    message.setText(mailBody.getText());

    mailSender.send(message);
  }
}
