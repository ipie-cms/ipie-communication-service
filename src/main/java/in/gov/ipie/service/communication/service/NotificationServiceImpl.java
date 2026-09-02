package in.gov.ipie.service.communication.service;

import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import in.gov.ipie.common.utils.masking.DataMasking;
import in.gov.ipie.service.communication.domain.NotificationChannel;
import in.gov.ipie.service.communication.event.AccountCredentialSetupRequestedEvent;
import in.gov.ipie.service.communication.event.RegistrationEmailOtpEvent;
import in.gov.ipie.service.communication.event.UserLoggedInEvent;
import in.gov.ipie.service.communication.event.UserRegistrationCompletedEvent;
import in.gov.ipie.service.communication.exception.MissingNotificationRecipientException;
import in.gov.ipie.service.communication.repository.NotificationLogRepository;
import in.gov.ipie.service.communication.repository.NotificationRecipientRepository;

/** {@link NotificationService} implementation. */
@Service
public class NotificationServiceImpl implements NotificationService {

    static final String USER_VERIFICATION_REQUEST_PURPOSE = "USER_VERIFICATION_REQUEST";
    static final String CREDENTIAL_SETUP_PURPOSE = "CREDENTIAL_SETUP";
    static final String REGISTRATION_RECEIVED_PURPOSE = "REGISTRATION_RECEIVED";
    static final String LOGIN_NOTIFICATION_PURPOSE = "LOGIN_NOTIFICATION";
    static final String REGISTRATION_EMAIL_OTP_PURPOSE = "REGISTRATION_EMAIL_OTP";

    private final NotificationRecipientRepository recipientRepository;
    private final NotificationLogRepository logRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final String userServicePublicUrl;
    private final String webAppPublicUrl;

    public NotificationServiceImpl(
            NotificationRecipientRepository recipientRepository,
            NotificationLogRepository logRepository,
            EmailService emailService,
            SmsService smsService,
            @Value("${ipie.notifications.user-service-public-url}") String userServicePublicUrl,
            @Value("${ipie.notifications.web-app-public-url}") String webAppPublicUrl) {
        this.recipientRepository = recipientRepository;
        this.logRepository = logRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.userServicePublicUrl = userServicePublicUrl;
        this.webAppPublicUrl = webAppPublicUrl;
    }

    @Override
    public void sendCredentialSetupLink(AccountCredentialSetupRequestedEvent event) {
        // Straight to the registrant's own address from the event - not through
        // recipientRepository. A per-purpose configured mailbox is right for the admin's approval
        // mail, which always goes to the same desk; it would be catastrophic here, where the whole
        // point is that the message reaches this one account's owner and nobody else.
        String setLink = webAppPublicUrl + "/set-password?token=" + event.setupToken();
        String subject = "iPIE: set your password";
        String body = """
                Hello %s,%n\
                %n\
                Your iPIE account has been created. Choose a password to finish setting it up:%n\
                %s%n\
                %n\
                This link can be used once and expires in 48 hours. If it has expired, you can%n\
                request a new one from the iPIE sign-in page.%n\
                %n\
                If you did not register with iPIE, ignore this email - no password will be set and%n\
                the account cannot be used.%n\
                """.formatted(event.fullName(), setLink);

        boolean sent = emailService.send(event.email(), subject, body);
        // The stored copy must never carry a working setup token (same DPDP masking rule as the
        // verification token below). Anyone able to read the notification log would otherwise be
        // able to set the password on every account that has registered recently.
        String loggedBody = body.replace(event.setupToken(), DataMasking.mask(event.setupToken(), 0));
        logRepository.record(CREDENTIAL_SETUP_PURPOSE, event.email(), subject, loggedBody, sent, "EMAIL");
    }

    @Override
    public void sendVerificationRequest(UserRegistrationCompletedEvent event) {
        String recipientEmail = recipientRepository.findEmailByPurpose(USER_VERIFICATION_REQUEST_PURPOSE)
                .orElseThrow(() -> new MissingNotificationRecipientException(USER_VERIFICATION_REQUEST_PURPOSE));

        String verifyLink = userServicePublicUrl + "/api/v1/users/verify?token=" + event.verificationToken();
        String subject = "iPIE: verify new registration - " + event.fullName();
        // %n (not a literal newline in the text block) per SpotBugs' VA_FORMAT_STRING_USES_NEWLINE -
        // the platform-specific line separator is correct for an outbound SMTP message body.
        String body = """
                A new user has completed registration and is awaiting verification.%n\
                %n\
                Name: %s%n\
                Email: %s%n\
                Mobile: %s%n\
                %n\
                Click the link below to verify this user:%n\
                %s%n\
                %n\
                This link expires per ipie.registration.verification-token-ttl (48 hours by default).%n\
                """.formatted(event.fullName(), event.email(), event.mobileNumber(), verifyLink);

        boolean sent = emailService.send(recipientEmail, subject, body);
        // The stored copy must never carry a working one-time verification token (DPDP masking
        // rule) - only the emailed copy above does. Masking the token substring specifically
        // (rather than the whole body) keeps the rest of the message readable for audit.
        String loggedBody = body.replace(event.verificationToken(), DataMasking.mask(event.verificationToken(), 0));
        logRepository.record(USER_VERIFICATION_REQUEST_PURPOSE, recipientEmail, subject, loggedBody, sent, "EMAIL");
    }

    @Override
    public void sendEmailOtp(RegistrationEmailOtpEvent event) {
        String subject = "iPIE: your registration verification code";
        String body = """
                Your iPIE registration verification code is: %s%n\
                %n\
                This code expires in 10 minutes. If you didn't request this, you can ignore this email.%n\
                """.formatted(event.code());

        boolean sent = emailService.send(event.email(), subject, body);
        // Same DPDP masking rule as sendVerificationRequest's token - the stored copy must never
        // carry a working OTP code, only the emailed copy above does.
        String loggedBody = body.replace(event.code(), DataMasking.mask(event.code(), 0));
        logRepository.record(REGISTRATION_EMAIL_OTP_PURPOSE, event.email(), subject, loggedBody, sent, "EMAIL");
    }

    @Override
    public void sendRegistrationReceivedNotification(UserRegistrationCompletedEvent event) {
        String subject = "iPIE: registration received";
        String emailBody = """
                Hello %s,%n\
                %n\
                Your iPIE registration was received and is pending pillar-admin approval.%n\
                You'll be notified once it's verified.%n\
                """.formatted(event.fullName());
        String smsBody = "iPIE: Your registration was received and is pending approval.";

        dispatch(
                event.notificationChannels(), event.email(), event.mobileNumber(), REGISTRATION_RECEIVED_PURPOSE, subject, emailBody,
                smsBody);
    }

    @Override
    public void sendLoginNotification(UserLoggedInEvent event) {
        String occurredAt = DateTimeFormatter.ISO_INSTANT.format(event.occurredAt());
        String subject = "iPIE: new login to your account";
        String emailBody = """
                Hello %s,%n\
                %n\
                We noticed a login to your iPIE account at %s from %s.%n\
                If this wasn't you, please contact support immediately.%n\
                """.formatted(event.fullName(), occurredAt, event.sourceIp());
        String smsBody = "iPIE: A login to your account was detected at " + occurredAt
                + " from " + event.sourceIp() + ". If this wasn't you, contact support.";

        dispatch(event.notificationChannels(), event.email(), event.mobileNumber(), LOGIN_NOTIFICATION_PURPOSE, subject, emailBody,
                smsBody);
    }

    private void dispatch(
            Set<NotificationChannel> channels, String email, String mobileNumber, String purpose, String subject,
            String emailBody, String smsBody) {
        for (NotificationChannel channel : channels) {
            switch (channel) {
                case EMAIL -> {
                    boolean sent = emailService.send(email, subject, emailBody);
                    logRepository.record(purpose, email, subject, emailBody, sent, "EMAIL");
                }
                case SMS -> {
                    boolean sent = smsService.send(mobileNumber, smsBody);
                    logRepository.record(purpose, mobileNumber, subject, smsBody, sent, "SMS");
                }
            }
        }
    }
}
