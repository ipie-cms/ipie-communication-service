package in.gov.ipie.service.communication.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import in.gov.ipie.common.events.envelope.EventEnvelope;
import in.gov.ipie.common.events.idempotency.IdempotentEventHandler;
import in.gov.ipie.common.events.idempotency.ProcessedEventStore;
import in.gov.ipie.service.communication.service.NotificationService;
import in.gov.ipie.service.communication.event.UserRegistrationCompletedEvent;

/**
 * Sends the pillar-admin verification email when ipie-user-service publishes {@code
 * USER_REGISTRATION_COMPLETED} (see {@code UserServiceEventConsumerConfig}). RabbitMQ-only for now
 * (matches the only broker actually active in docker-compose today).
 */
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
class UserRegistrationCompletedEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(UserRegistrationCompletedEventConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventStore processedEventStore;

    UserRegistrationCompletedEventConsumer(NotificationService notificationService, ProcessedEventStore processedEventStore) {
        this.notificationService = notificationService;
        this.processedEventStore = processedEventStore;
    }

    @RabbitListener(queues = "${ipie.integrations.user-service.rabbitmq.registration-completed-queue:"
            + "ipie-communication-service.events.registration-completed}")
    void onUserRegistrationCompleted(EventEnvelope<UserRegistrationCompletedEvent> event) {
        IdempotentEventHandler.handle(event.eventId(), processedEventStore, () -> {
            LOG.info("Sending verification-request email for user {}", event.data().userId());
            notificationService.sendVerificationRequest(event.data());
            notificationService.sendRegistrationReceivedNotification(event.data());
        });
    }
}
