package in.gov.ipie.service.communication.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import in.gov.ipie.common.events.envelope.EventEnvelope;
import in.gov.ipie.common.events.idempotency.IdempotentEventHandler;
import in.gov.ipie.common.events.idempotency.ProcessedEventStore;
import in.gov.ipie.service.communication.event.AccountCredentialSetupRequestedEvent;
import in.gov.ipie.service.communication.service.NotificationService;

/**
 * Emails a registrant the one-time link for choosing their first password, on ipie-iam-service's
 * request.
 *
 * <p>This is the step whose absence dead-ended the previous design: the account was created without
 * credentials and nothing ever sent its owner a way to set one. The only email carrying a token went
 * to the pillar-admin mailbox, which approves registrations - a different person, a different
 * power. This consumer is the registrant's own message.
 *
 * <p>Note the deliberate log line: it names the user, never the token. The token in the payload can
 * take the account over, so it belongs in the email body and nowhere else.
 */
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
class AccountCredentialSetupRequestedEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(AccountCredentialSetupRequestedEventConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventStore processedEventStore;

    AccountCredentialSetupRequestedEventConsumer(
            NotificationService notificationService, ProcessedEventStore processedEventStore) {
        this.notificationService = notificationService;
        this.processedEventStore = processedEventStore;
    }

    @RabbitListener(queues = "${ipie.integrations.iam-service.rabbitmq.credential-setup-queue:"
            + "ipie-communication-service.events.credential-setup-requested}")
    void onCredentialSetupRequested(EventEnvelope<AccountCredentialSetupRequestedEvent> event) {
        IdempotentEventHandler.handle(event.eventId(), processedEventStore, () -> {
            LOG.info("Sending set-password link for user {}", event.data().userId());
            notificationService.sendCredentialSetupLink(event.data());
        });
    }
}
