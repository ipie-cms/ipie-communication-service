package in.gov.ipie.service.communication.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import in.gov.ipie.common.events.envelope.EventEnvelope;
import in.gov.ipie.common.events.idempotency.ProcessedEventStore;
import in.gov.ipie.service.communication.domain.NotificationChannel;
import in.gov.ipie.service.communication.event.UserRegistrationCompletedEvent;
import in.gov.ipie.service.communication.service.NotificationService;

class KafkaUserRegistrationCompletedEventConsumerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final ProcessedEventStore processedEventStore = mock(ProcessedEventStore.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaUserRegistrationCompletedEventConsumer consumer =
            new KafkaUserRegistrationCompletedEventConsumer(notificationService, processedEventStore, objectMapper);

    @Test
    void onEvent_sendsBothNotifications_forAMatchingEventType() {
        UserRegistrationCompletedEvent payload = new UserRegistrationCompletedEvent(
                UUID.randomUUID(), "jane@example.com", "Jane Doe", "+91 9800000009", "tok-123", Set.of(NotificationChannel.EMAIL));
        EventEnvelope<UserRegistrationCompletedEvent> event =
                EventEnvelope.create("USER_REGISTRATION_COMPLETED", 1, "ipie-user-service", null, null, payload);
        when(processedEventStore.isProcessed(event.eventId())).thenReturn(false);

        consumer.onEvent(event);

        verify(notificationService).sendVerificationRequest(payload);
        verify(notificationService).sendRegistrationReceivedNotification(payload);
    }

    @Test
    void onEvent_ignoresAnyOtherEventTypeOnTheSharedTopic() {
        EventEnvelope<String> event = EventEnvelope.create("USER_CREATED", 1, "ipie-user-service", null, null, "user-1");

        consumer.onEvent(event);

        verify(notificationService, never()).sendVerificationRequest(any());
        verify(notificationService, never()).sendRegistrationReceivedNotification(any());
    }
}
