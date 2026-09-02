package in.gov.ipie.service.communication.service;

import in.gov.ipie.service.communication.event.AccountCredentialSetupRequestedEvent;
import in.gov.ipie.service.communication.event.RegistrationEmailOtpEvent;
import in.gov.ipie.service.communication.event.UserLoggedInEvent;
import in.gov.ipie.service.communication.event.UserRegistrationCompletedEvent;

/**
 * Orchestrates "who gets notified about what" - {@link EmailService}/{@link SmsService} only know
 * how to send one message over one channel each. See {@link NotificationServiceImpl} for the
 * implementation - the interface exists so callers depend on a contract rather than a concrete
 * class.
 */
public interface NotificationService {

    /**
     * Emails the <b>pillar admin</b> - at the address configured for the
     * {@code USER_VERIFICATION_REQUEST} purpose, not the registrant's - a one-click link to approve
     * a newly-registered user.
     */
    void sendVerificationRequest(UserRegistrationCompletedEvent event);

    /**
     * Emails the <b>registrant themselves</b> the one-time link for choosing their first password.
     *
     * <p>Distinct from {@link #sendVerificationRequest} in both recipient and power: that one lets an
     * admin approve a registration, this one lets the account's owner take control of it. Neither
     * token may appear in the other's message.
     */
    void sendCredentialSetupLink(AccountCredentialSetupRequestedEvent event);

    /** Emails the registrant themselves a fresh OTP code, requested by the registration wizard's "SEND OTP" button. */
    void sendEmailOtp(RegistrationEmailOtpEvent event);

    /** Notifies the registrant themselves, over their opted channel(s), that registration was received. */
    void sendRegistrationReceivedNotification(UserRegistrationCompletedEvent event);

    /** Notifies the account owner, over their opted channel(s), that a login to their account was detected. */
    void sendLoginNotification(UserLoggedInEvent event);
}
