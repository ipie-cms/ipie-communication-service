package in.gov.ipie.service.communication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 * {@link SmsService} placeholder - no SMS gateway vendor has been chosen for this platform yet
 * (confirmed: no Twilio/MSG91/SNS credentials or client exist anywhere in this codebase). Logs
 * the would-be message at INFO and reports success, so every caller-side behaviour (dispatch
 * logic, notification_log rows) is already exercised end to end; replacing this with a real
 * gateway later is a drop-in swap of this one class, no change to {@link NotificationService} or
 * any consumer. Never silently pretends a message reached a phone - the log line makes clear this
 * is a placeholder, not a real send (see {@code EmailServiceImpl}'s Javadoc for the contrasting
 * "never a logging-only stub" precedent this class deliberately does not follow, since here there
 * is genuinely no vendor to wire yet).
 */
@Service
public class LoggingSmsServiceImpl implements SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingSmsServiceImpl.class);

    @Override
    public boolean send(String toNumber, String message) {
        LOG.info("[SMS placeholder - no vendor wired yet] Would send to {}: {}", toNumber, message);
        return true;
    }
}
