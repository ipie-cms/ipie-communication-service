package in.gov.ipie.service.communication.service;

/**
 * Sends a single SMS. See {@link LoggingSmsServiceImpl} for the current implementation - a
 * logging-only placeholder pending a real vendor (Twilio/MSG91/AWS SNS, ...), swappable behind
 * this interface with no change to any caller once one is chosen. The interface exists so callers
 * depend on a contract rather than a concrete class, matching {@link EmailService}'s shape.
 */
public interface SmsService {

    /** Returns whether the send succeeded - the caller ({@link NotificationService}) records this either way. */
    boolean send(String toNumber, String message);
}
