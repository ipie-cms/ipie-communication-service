package in.gov.ipie.service.communication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import in.gov.ipie.service.communication.domain.NotificationChannel;
import in.gov.ipie.service.communication.event.AccountCredentialSetupRequestedEvent;
import in.gov.ipie.service.communication.event.RegistrationEmailOtpEvent;
import in.gov.ipie.service.communication.event.UserLoggedInEvent;
import in.gov.ipie.service.communication.event.UserRegistrationCompletedEvent;
import in.gov.ipie.service.communication.exception.MissingNotificationRecipientException;
import in.gov.ipie.service.communication.exception.NotificationErrorCode;
import in.gov.ipie.service.communication.repository.NotificationLogRepository;
import in.gov.ipie.service.communication.repository.NotificationRecipientRepository;

class NotificationServiceImplTest {

    private final NotificationRecipientRepository recipientRepository = mock(NotificationRecipientRepository.class);
    private final NotificationLogRepository logRepository = mock(NotificationLogRepository.class);
    private final EmailService emailService = mock(EmailService.class);
    private final SmsService smsService = mock(SmsService.class);
    private final NotificationServiceImpl notificationService = new NotificationServiceImpl(
            recipientRepository, logRepository, emailService, smsService,
            "http://localhost:8092", "http://localhost:5173");

    /**
     * The regression that matters most in this class. The previous design created accounts without
     * credentials and never sent their owners a way to set one - the only email carrying a token
     * went to the pillar-admin mailbox. This asserts the registrant's own address is used, and
     * pointedly that the configured-recipient lookup is *not* consulted.
     */
    @Test
    void sendCredentialSetupLink_emailsTheRegistrantThemselves_notTheConfiguredAdminMailbox() {
        when(emailService.send(any(), any(), any())).thenReturn(true);

        AccountCredentialSetupRequestedEvent event = new AccountCredentialSetupRequestedEvent(
                UUID.randomUUID(), UUID.randomUUID(), "newcomer@example.com", "Jane Doe", "setup-tok-123");
        notificationService.sendCredentialSetupLink(event);

        verify(emailService).send(
                eq("newcomer@example.com"), any(), contains("http://localhost:5173/set-password?token=setup-tok-123"));
        verify(recipientRepository, never()).findEmailByPurpose(any());
        verify(logRepository).record(eq("CREDENTIAL_SETUP"), eq("newcomer@example.com"), any(), any(), eq(true), eq("EMAIL"));
    }

    @Test
    void sendCredentialSetupLink_masksTheSetupTokenInTheLoggedBody_butNotInTheEmailedBody() {
        when(emailService.send(any(), any(), any())).thenReturn(true);

        AccountCredentialSetupRequestedEvent event = new AccountCredentialSetupRequestedEvent(
                UUID.randomUUID(), UUID.randomUUID(), "newcomer@example.com", "Jane Doe", "setup-tok-123");
        notificationService.sendCredentialSetupLink(event);

        ArgumentCaptor<String> emailedBody = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(any(), any(), emailedBody.capture());
        ArgumentCaptor<String> loggedBody = ArgumentCaptor.forClass(String.class);
        verify(logRepository).record(any(), any(), any(), loggedBody.capture(), eq(true), any());

        assertThat(emailedBody.getValue()).contains("setup-tok-123");
        // A readable token in the notification log would let anyone with read access set the
        // password on every recently-registered account.
        assertThat(loggedBody.getValue()).doesNotContain("setup-tok-123");
    }

    @Test
    void sendVerificationRequest_emailsConfiguredRecipientWithVerifyLink_andLogsSuccess() {
        when(recipientRepository.findEmailByPurpose("USER_VERIFICATION_REQUEST")).thenReturn(Optional.of("admin@ipie.gov.in"));
        when(emailService.send(eq("admin@ipie.gov.in"), any(), contains("http://localhost:8092/api/v1/users/verify?token=tok-123")))
                .thenReturn(true);

        UserRegistrationCompletedEvent event = new UserRegistrationCompletedEvent(
                UUID.randomUUID(), "newcomer@example.com", "Jane Doe", "+91 9800000009", "tok-123", Set.of(NotificationChannel.EMAIL));
        notificationService.sendVerificationRequest(event);

        verify(logRepository).record(eq("USER_VERIFICATION_REQUEST"), eq("admin@ipie.gov.in"), any(), any(), eq(true), eq("EMAIL"));
    }

    @Test
    void sendVerificationRequest_logsFailure_whenEmailSendFails() {
        when(recipientRepository.findEmailByPurpose("USER_VERIFICATION_REQUEST")).thenReturn(Optional.of("admin@ipie.gov.in"));
        when(emailService.send(any(), any(), any())).thenReturn(false);

        UserRegistrationCompletedEvent event = new UserRegistrationCompletedEvent(
                UUID.randomUUID(), "newcomer@example.com", "Jane Doe", "+91 9800000009", "tok-123", Set.of(NotificationChannel.EMAIL));
        notificationService.sendVerificationRequest(event);

        verify(logRepository).record(eq("USER_VERIFICATION_REQUEST"), eq("admin@ipie.gov.in"), any(), any(), eq(false), eq("EMAIL"));
    }

    @Test
    void sendVerificationRequest_masksTheVerificationTokenInTheLoggedBody_butNotInTheEmailedBody() {
        when(recipientRepository.findEmailByPurpose("USER_VERIFICATION_REQUEST")).thenReturn(Optional.of("admin@ipie.gov.in"));
        when(emailService.send(any(), any(), any())).thenReturn(true);

        UserRegistrationCompletedEvent event = new UserRegistrationCompletedEvent(
                UUID.randomUUID(), "newcomer@example.com", "Jane Doe", "+91 9800000009", "tok-123", Set.of(NotificationChannel.EMAIL));
        notificationService.sendVerificationRequest(event);

        ArgumentCaptor<String> emailedBody = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("admin@ipie.gov.in"), any(), emailedBody.capture());
        assertThat(emailedBody.getValue()).contains("token=tok-123");

        ArgumentCaptor<String> loggedBody = ArgumentCaptor.forClass(String.class);
        verify(logRepository).record(
                eq("USER_VERIFICATION_REQUEST"), eq("admin@ipie.gov.in"), any(), loggedBody.capture(), eq(true), eq("EMAIL"));
        assertThat(loggedBody.getValue()).doesNotContain("tok-123").contains("*******");
    }

    @Test
    void sendVerificationRequest_throws_whenNoRecipientConfigured() {
        when(recipientRepository.findEmailByPurpose("USER_VERIFICATION_REQUEST")).thenReturn(Optional.empty());

        UserRegistrationCompletedEvent event = new UserRegistrationCompletedEvent(
                UUID.randomUUID(), "newcomer@example.com", "Jane Doe", "+91 9800000009", "tok-123", Set.of(NotificationChannel.EMAIL));

        MissingNotificationRecipientException thrown = catchThrowableOfType(
                MissingNotificationRecipientException.class, () -> notificationService.sendVerificationRequest(event));

        assertThat(thrown.errorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_RECIPIENT_NOT_CONFIGURED);
        assertThat(thrown).hasMessageContaining("USER_VERIFICATION_REQUEST");
    }

    @Test
    void sendEmailOtp_masksTheCodeInTheLoggedBody_butNotInTheEmailedBody() {
        when(emailService.send(any(), any(), any())).thenReturn(true);

        RegistrationEmailOtpEvent event = new RegistrationEmailOtpEvent(UUID.randomUUID(), "newcomer@example.com", "654321");
        notificationService.sendEmailOtp(event);

        ArgumentCaptor<String> emailedBody = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("newcomer@example.com"), any(), emailedBody.capture());
        assertThat(emailedBody.getValue()).contains("654321");

        ArgumentCaptor<String> loggedBody = ArgumentCaptor.forClass(String.class);
        verify(logRepository).record(
                eq("REGISTRATION_EMAIL_OTP"), eq("newcomer@example.com"), any(), loggedBody.capture(), eq(true), eq("EMAIL"));
        assertThat(loggedBody.getValue()).doesNotContain("654321").contains("*");
    }

    @Test
    void sendRegistrationReceivedNotification_dispatchesOverEveryOptedChannel() {
        when(emailService.send(eq("newcomer@example.com"), any(), any())).thenReturn(true);
        when(smsService.send(eq("+91 9800000009"), any())).thenReturn(true);

        UserRegistrationCompletedEvent event = new UserRegistrationCompletedEvent(
                UUID.randomUUID(), "newcomer@example.com", "Jane Doe", "+91 9800000009", "tok-123",
                Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS));
        notificationService.sendRegistrationReceivedNotification(event);

        verify(emailService).send(eq("newcomer@example.com"), any(), any());
        verify(smsService).send(eq("+91 9800000009"), any());
        verify(logRepository).record(eq("REGISTRATION_RECEIVED"), eq("newcomer@example.com"), any(), any(), eq(true), eq("EMAIL"));
        verify(logRepository).record(eq("REGISTRATION_RECEIVED"), eq("+91 9800000009"), any(), any(), eq(true), eq("SMS"));
    }

    @Test
    void sendRegistrationReceivedNotification_onlyDispatchesOverOptedChannels() {
        UserRegistrationCompletedEvent event = new UserRegistrationCompletedEvent(
                UUID.randomUUID(), "newcomer@example.com", "Jane Doe", "+91 9800000009", "tok-123", Set.of(NotificationChannel.EMAIL));
        notificationService.sendRegistrationReceivedNotification(event);

        verify(smsService, never()).send(any(), any());
    }

    @Test
    void sendLoginNotification_dispatchesOverEveryOptedChannel() {
        when(emailService.send(eq("jane@example.com"), any(), any())).thenReturn(true);
        when(smsService.send(eq("+91 9800000009"), any())).thenReturn(true);

        UserLoggedInEvent event = new UserLoggedInEvent(
                UUID.randomUUID(), "jane@example.com", "+91 9800000009", "Jane Doe",
                Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS), Instant.parse("2026-01-01T10:00:00Z"), "203.0.113.5");
        notificationService.sendLoginNotification(event);

        verify(logRepository).record(eq("LOGIN_NOTIFICATION"), eq("jane@example.com"), any(), any(), eq(true), eq("EMAIL"));
        verify(logRepository).record(eq("LOGIN_NOTIFICATION"), eq("+91 9800000009"), any(), any(), eq(true), eq("SMS"));
    }
}
