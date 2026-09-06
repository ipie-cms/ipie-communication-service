package in.gov.ipie.service.communication.exception;

import in.gov.ipie.common.core.exception.IpieException;

/**
 * No {@code notification_recipients} row exists for a purpose whose notification is addressed via
 * the distribution list rather than to the user directly (today only
 * {@code USER_VERIFICATION_REQUEST}).
 *
 * <p>This is an <b>operator misconfiguration</b>, not a caller error: the list is seeded solely by
 * {@code V7__seed_dummy_recipients.sql} and no API or admin screen can repair it at runtime.
 * Replaces a raw {@code IllegalStateException} so the condition carries a stable
 * {@link NotificationErrorCode} that can be alerted on, and so it satisfies the ArchUnit rule that
 * every domain exception extend {@link IpieException}.
 *
 * <p>It is raised only on the event-consumer path, so the HTTP mapping is currently moot. Were it
 * ever to surface through a controller, {@code GlobalExceptionHandler}'s catch-all would render it
 * 422, which would understate it as a caller error - common-libs offers no domain exception that
 * maps to 5xx, and inventing a service-local one would fragment the shared error contract.
 */
public class MissingNotificationRecipientException extends IpieException {

    public MissingNotificationRecipientException(String purpose) {
        super(
                NotificationErrorCode.NOTIFICATION_RECIPIENT_NOT_CONFIGURED,
                "No notification_recipients row configured for purpose " + purpose);
    }
}
