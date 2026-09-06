package in.gov.ipie.service.communication.service;

/**
 * Sends a single email. See {@link EmailServiceImpl} for the implementation - the interface
 * exists so callers depend on a contract rather than a concrete class.
 */
public interface EmailService {

    /** Returns whether the send succeeded - the caller ({@link NotificationService}) records this either way. */
    boolean send(String toAddress, String subject, String body);
}
