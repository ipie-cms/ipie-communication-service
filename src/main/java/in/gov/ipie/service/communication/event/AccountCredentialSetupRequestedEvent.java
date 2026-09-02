package in.gov.ipie.service.communication.event;

import java.util.UUID;

/**
 * Mirrors ipie-iam-service's {@code AccountCredentialSetupRequestedPayload} - a local, duck-typed
 * copy of the publisher's shape, since services do not share a contracts module on this platform.
 *
 * <p>{@code setupToken} is a bearer secret: it lets whoever holds it choose this account's password.
 * It belongs in exactly one place - the body of the email sent to {@code email} - and must never
 * reach a log or the stored notification-log copy. See {@code NotificationServiceImpl}, which masks
 * it before recording the message.
 */
public record AccountCredentialSetupRequestedEvent(
        UUID userId,
        UUID keycloakUserId,
        String email,
        String fullName,
        String setupToken) {
}
