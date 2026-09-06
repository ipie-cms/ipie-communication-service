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
import in.gov.ipie.service.communication.event.RegistrationEmailOtpPayload;

/**
 * Sends the registration wizard's email OTP when ipie-user-service publishes {@code
 * REGISTRATION_EMAIL_OTP_REQUESTED} (see {@code UserServiceEventConsumerConfig}). RabbitMQ-only
 * for now, same convention as {@link UserRegistrationCompletedConsumer}.
 */
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
class RegistrationEmailOtpConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationEmailOtpConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventStore processedEventStore;

    RegistrationEmailOtpConsumer(NotificationService notificationService, ProcessedEventStore processedEventStore) {
        this.notificationService = notificationService;
        this.processedEventStore = processedEventStore;
    }

    @RabbitListener(queues = "${ipie.integrations.user-service.rabbitmq.registration-email-otp-queue:"
            + "ipie-communication-service.events.registration-email-otp}")
    void onRegistrationEmailOtpRequested(EventEnvelope<RegistrationEmailOtpPayload> event) {
        IdempotentEventHandler.handle(event.eventId(), processedEventStore, () -> {
            LOG.info("Sending registration email OTP for user {}", event.data().userId());
            notificationService.sendEmailOtp(event.data());
        });
    }
}
