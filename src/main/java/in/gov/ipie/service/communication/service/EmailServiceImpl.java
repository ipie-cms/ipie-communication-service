package in.gov.ipie.service.communication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * {@link EmailService} implementation. Thin wrapper over {@link JavaMailSender} - real SMTP in
 * every environment (MailHog in local dev, per docker-compose.yml; a real SMTP/SES relay in
 * production via {@code spring.mail.*}), never a logging-only stub, so "the email was sent" is
 * genuinely true end to end.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender, @Value("${ipie.notifications.from-address}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public boolean send(String toAddress, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            return true;
        } catch (MailException e) {
            LOG.error("Failed to send email to {}: {}", toAddress, e.getMessage());
            return false;
        }
    }
}
