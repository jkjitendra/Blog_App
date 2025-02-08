package com.jk.blog.service;

import com.jk.blog.dto.MailBody;
import com.jk.blog.exception.EmailSendingException;
import com.jk.blog.service.impl.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private MimeMessageHelper mimeMessageHelper;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MailBody mailBody;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Manually set fromEmail since it comes from Vault
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@example.com");

        mailBody = new MailBody();
        mailBody.setTo("recipient@example.com");
        mailBody.setSubject("Test Subject");
        mailBody.setText("Test Email Body");

        mimeMessage = mock(MimeMessage.class);

    }

    /**
     * Test case: Successfully send an email
     */
    @Test
    void test_sendEmail_ShouldSendEmail_WhenValidInput() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.sendEmail(mailBody));

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    /**
     * Test case: Throw EmailSendingException when email sending fails due to MailException
     */
    @Test
    void test_sendEmail_ShouldThrowEmailSendingException_WhenMailExceptionOccurs() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailException("Mail send failed") {}).when(mailSender).send(any(MimeMessage.class));

        EmailSendingException exception = assertThrows(EmailSendingException.class, () -> emailService.sendEmail(mailBody));

        assertEquals(exception.getMessage(), "Failed to send email to recipient@example.com. Reason: Mail send failed");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

}
