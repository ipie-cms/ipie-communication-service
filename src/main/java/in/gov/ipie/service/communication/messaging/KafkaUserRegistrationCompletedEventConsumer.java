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
import in.gov.ipie.service.communication.event.UserRegistrationCompletedEvent;
import in.gov.ipie.service.communication.service.NotificationService;

/**
 * Kafka counterpart to {@link UserRegistrationCompletedEventConsumer} - active only when this
 * service is configured to use Kafka instead of RabbitMQ.
 *
 * <p>Unlike the RabbitMQ side (a dedicated queue bound to just this routing key), a Kafka topic
 * carries every event type ipie-user-service publishes - {@code EventConsumerConfig}'s consumer
 * factory always deserializes into a raw {@link EventEnvelope}{@code <?>} (the same
 * generic-payload shape {@code UserEventLogConsumer} already receives), so this listener
 * subscribes to the whole topic and filters to {@code USER_REGISTRATION_COMPLETED} itself,
 * converting the matching payload with {@code ObjectMapper.convertValue(...)} rather than {@code
 * readValue(...)} - {@code event.data()} arrives already deserialized once (a {@code
 * LinkedHashMap} at this generic boundary), so this is an object-graph conversion, not a
 * JSON-string parse (same reasoning as {@code AuditEventCodec}).
 */
@Component
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
class KafkaUserRegistrationCompletedEventConsumer {

    private static final String EVENT_TYPE = "USER_REGISTRATION_COMPLETED";

    private static final Logger LOG = LoggerFactory.getLogger(KafkaUserRegistrationCompletedEventConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventStore processedEventStore;
    private final ObjectMapper objectMapper;

    KafkaUserRegistrationCompletedEventConsumer(
            NotificationService notificationService, ProcessedEventStore processedEventStore, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.processedEventStore = processedEventStore;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${ipie.integrations.user-service.kafka.topic:ipie-user-service.events}",
            groupId = "${spring.application.name}.registration-completed")
    void onEvent(EventEnvelope<?> event) {
        if (!EVENT_TYPE.equals(event.eventType())) {
            return;
        }
        IdempotentEventHandler.handle(event.eventId(), processedEventStore, () -> {
            UserRegistrationCompletedEvent payload =
                    objectMapper.convertValue(event.data(), UserRegistrationCompletedEvent.class);
            LOG.info("Sending verification-request email for user {}", payload.userId());
            notificationService.sendVerificationRequest(payload);
            notificationService.sendRegistrationReceivedNotification(payload);
        });
    }
}
