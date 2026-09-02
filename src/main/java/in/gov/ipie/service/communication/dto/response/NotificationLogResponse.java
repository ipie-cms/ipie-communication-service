package in.gov.ipie.service.communication.dto.response;

import java.time.Instant;

public record NotificationLogResponse(
        String id, String purpose, String recipient, String subject, String body, String status, String channel,
        Instant sentAt) {
}
