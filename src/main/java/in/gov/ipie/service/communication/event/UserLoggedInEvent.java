package in.gov.ipie.service.communication.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import in.gov.ipie.service.communication.domain.NotificationChannel;

/**
 * Mirrors ipie-user-service's {@code UserLoggedInPayload} - the shape of the {@code
 * EventEnvelope.data} field for the {@code USER_LOGGED_IN} event this service consumes. Services
 * do not share a contracts module in this platform - this is a local, duck-typed copy of the
 * publisher's payload shape.
 */
public record UserLoggedInEvent(
        UUID userId, String email, String mobileNumber, String fullName, Set<NotificationChannel> notificationChannels,
        Instant occurredAt, String sourceIp) {
}
