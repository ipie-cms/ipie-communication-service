package in.gov.ipie.service.communication.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import in.gov.ipie.common.events.envelope.EventEnvelope;
import in.gov.ipie.common.events.idempotency.IdempotentEventHandler;
import in.gov.ipie.common.events.idempotency.ProcessedEventStore;
import in.gov.ipie.service.communication.event.RegistrationEmailOtpEvent;
import in.gov.ipie.service.communication.service.NotificationService;

/**
 * Kafka counterpart to {@link RegistrationEmailOtpEventConsumer} - active only when this service
 * is configured to use Kafka instead of RabbitMQ. Same filter-the-whole-topic shape as {@link
 * KafkaUserRegistrationCompletedEventConsumer} - see that class's Javadoc.
 */
@Component
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
class KafkaRegistrationEmailOtpEventConsumer {

    private static final String EVENT_TYPE = "REGISTRATION_EMAIL_OTP_REQUESTED";

    private static final Logger LOG = LoggerFactory.getLogger(KafkaRegistrationEmailOtpEventConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventStore processedEventStore;
    private final ObjectMapper objectMapper;

    KafkaRegistrationEmailOtpEventConsumer(
            NotificationService notificationService, ProcessedEventStore processedEventStore, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.processedEventStore = processedEventStore;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${ipie.integrations.user-service.kafka.topic:ipie-user-service.events}",
            groupId = "${spring.application.name}.registration-email-otp")
    void onEvent(EventEnvelope<?> event) {
        if (!EVENT_TYPE.equals(event.eventType())) {
            return;
        }
        IdempotentEventHandler.handle(event.eventId(), processedEventStore, () -> {
            RegistrationEmailOtpEvent payload = objectMapper.convertValue(event.data(), RegistrationEmailOtpEvent.class);
            LOG.info("Sending registration email OTP for user {}", payload.userId());
            notificationService.sendEmailOtp(payload);
        });
    }
}
