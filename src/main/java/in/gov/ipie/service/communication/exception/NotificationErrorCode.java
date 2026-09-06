package in.gov.ipie.service.communication.exception;

import in.gov.ipie.common.core.exception.ErrorCode;

/** Stable, service-specific error codes for the Notification domain (master standards doc, 5.4). */
public enum NotificationErrorCode implements ErrorCode {

    /** No {@code notification_recipients} row is configured for a purpose that requires one. */
    NOTIFICATION_RECIPIENT_NOT_CONFIGURED;

    @Override
    public String code() {
        return name();
    }
}
