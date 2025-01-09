package com.jk.blog.service;

import com.jk.blog.dto.MailBody;

public interface EmailService {

    void sendEmail(MailBody mailBody);
}
