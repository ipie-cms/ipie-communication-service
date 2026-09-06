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
import in.gov.ipie.service.communication.event.UserLoggedInPayload;
import in.gov.ipie.service.communication.service.NotificationService;

/**
 * Kafka counterpart to {@link UserLoggedInConsumer} - active only when this service is
 * configured to use Kafka instead of RabbitMQ. See {@link
 * KafkaUserRegistrationCompletedConsumer}'s Javadoc for why this subscribes to the whole
 * topic and filters in-method, rather than a dedicated queue per event type the way the RabbitMQ
 * side works.
 */
@Component
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
class KafkaUserLoggedInConsumer {

    private static final String EVENT_TYPE = "USER_LOGGED_IN";

    private static final Logger LOG = LoggerFactory.getLogger(KafkaUserLoggedInConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventStore processedEventStore;
    private final ObjectMapper objectMapper;

    KafkaUserLoggedInConsumer(
            NotificationService notificationService, ProcessedEventStore processedEventStore, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.processedEventStore = processedEventStore;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${ipie.integrations.user-service.kafka.topic:ipie-user-service.events}",
            groupId = "${spring.application.name}.logged-in")
    void onEvent(EventEnvelope<?> event) {
        if (!EVENT_TYPE.equals(event.eventType())) {
            return;
        }
        IdempotentEventHandler.handle(event.eventId(), processedEventStore, () -> {
            UserLoggedInPayload payload = objectMapper.convertValue(event.data(), UserLoggedInPayload.class);
            LOG.info("Sending login notification for user {}", payload.userId());
            notificationService.sendLoginNotification(payload);
        });
    }
}
