package in.gov.ipie.service.communication.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A single previously-sent notification, read back from the append-only {@code notification_log}
 * audit trail (see {@code NotificationLogRepository}). {@code recipient} is an email address or a
 * phone number depending on {@code channel}, matching the JPA entity's own field.
 */
public record NotificationLog(
        UUID id, String purpose, String recipient, String subject, String body, String status, String channel,
        Instant sentAt, Instant createdAt) {
}
