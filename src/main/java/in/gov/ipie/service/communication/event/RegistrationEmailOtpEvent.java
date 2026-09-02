package in.gov.ipie.service.communication.event;

import java.util.UUID;

/**
 * Mirrors ipie-user-service's {@code RegistrationEmailOtpRequestedPayload} - the shape of the
 * {@code EventEnvelope.data} field for the {@code REGISTRATION_EMAIL_OTP_REQUESTED} event this
 * service consumes. Services do not share a contracts module in this platform - this is a local,
 * duck-typed copy of the publisher's payload shape (same reasoning as {@link
 * UserRegistrationCompletedEvent}).
 */
public record RegistrationEmailOtpEvent(UUID userId, String email, String code) {
}
