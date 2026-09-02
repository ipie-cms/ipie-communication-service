package in.gov.ipie.service.communication.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.Test;

import in.gov.ipie.common.events.envelope.EventEnvelope;
import in.gov.ipie.common.events.idempotency.ProcessedEventStore;
import in.gov.ipie.service.communication.domain.NotificationChannel;
import in.gov.ipie.service.communication.event.UserLoggedInEvent;
import in.gov.ipie.service.communication.service.NotificationService;

class KafkaUserLoggedInEventConsumerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final ProcessedEventStore processedEventStore = mock(ProcessedEventStore.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final KafkaUserLoggedInEventConsumer consumer =
            new KafkaUserLoggedInEventConsumer(notificationService, processedEventStore, objectMapper);

    @Test
    void onEvent_sendsTheLoginNotification_forAMatchingEventType() {
        UserLoggedInEvent payload = new UserLoggedInEvent(
                UUID.randomUUID(), "jane@example.com", "+91 9800000009", "Jane Doe", Set.of(NotificationChannel.EMAIL),
                Instant.parse("2026-01-01T10:00:00Z"), "203.0.113.5");
        EventEnvelope<UserLoggedInEvent> event = EventEnvelope.create("USER_LOGGED_IN", 1, "ipie-user-service", null, null, payload);
        when(processedEventStore.isProcessed(event.eventId())).thenReturn(false);

        consumer.onEvent(event);

        verify(notificationService).sendLoginNotification(payload);
    }

    @Test
    void onEvent_ignoresAnyOtherEventTypeOnTheSharedTopic() {
        EventEnvelope<String> event = EventEnvelope.create("USER_CREATED", 1, "ipie-user-service", null, null, "user-1");

        consumer.onEvent(event);

        verify(notificationService, never()).sendLoginNotification(any());
    }
}
