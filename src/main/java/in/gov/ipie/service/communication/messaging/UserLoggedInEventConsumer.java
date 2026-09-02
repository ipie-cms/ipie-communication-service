package in.gov.ipie.service.communication.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import in.gov.ipie.common.events.envelope.EventEnvelope;
import in.gov.ipie.common.events.idempotency.IdempotentEventHandler;
import in.gov.ipie.common.events.idempotency.ProcessedEventStore;
import in.gov.ipie.service.communication.event.UserLoggedInEvent;
import in.gov.ipie.service.communication.service.NotificationService;

/**
 * Sends the "a login was detected" notification when ipie-user-service publishes {@code
 * USER_LOGGED_IN} (see {@code UserServiceEventConsumerConfig}). RabbitMQ-only for now, mirroring
 * {@link UserRegistrationCompletedEventConsumer}'s shape exactly (matches the only broker
 * actually active in docker-compose today).
 */
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
class UserLoggedInEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(UserLoggedInEventConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventStore processedEventStore;

    UserLoggedInEventConsumer(NotificationService notificationService, ProcessedEventStore processedEventStore) {
        this.notificationService = notificationService;
        this.processedEventStore = processedEventStore;
    }

    @RabbitListener(queues = "${ipie.integrations.user-service.rabbitmq.logged-in-queue:"
            + "ipie-communication-service.events.logged-in}")
    void onUserLoggedIn(EventEnvelope<UserLoggedInEvent> event) {
        IdempotentEventHandler.handle(event.eventId(), processedEventStore, () -> {
            LOG.info("Sending login notification for user {}", event.data().userId());
            notificationService.sendLoginNotification(event.data());
        });
    }
}
