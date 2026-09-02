package in.gov.ipie.service.communication.domain;

/**
 * Mirrors ipie-user-service's {@code NotificationChannel} - services do not share a contracts
 * module in this platform, this is a local, duck-typed copy (see {@code
 * UserRegistrationCompletedEvent}'s Javadoc for the same convention applied to whole payloads).
 * {@code EMAIL} is dispatched via the real {@code EmailService}; {@code SMS} via {@code
 * SmsService}'s current logging-only placeholder (no vendor chosen yet).
 */
public enum NotificationChannel {
    EMAIL,
    SMS
}
