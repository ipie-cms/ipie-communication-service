package in.gov.ipie.service.communication.event;

import java.util.Set;
import java.util.UUID;

import in.gov.ipie.service.communication.domain.NotificationChannel;

/**
 * Mirrors ipie-user-service's {@code UserRegistrationCompletedPayload} - the shape of the {@code
 * EventEnvelope.data} field for the {@code USER_REGISTRATION_COMPLETED} event this service
 * consumes. Services do not share a contracts module in this platform - this is a local,
 * duck-typed copy of the publisher's payload shape.
 */
public record UserRegistrationCompletedEvent(
        UUID userId, String email, String fullName, String mobileNumber, String verificationToken,
        Set<NotificationChannel> notificationChannels) {
}
