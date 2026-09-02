package in.gov.ipie.service.communication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Sends the pillar-admin verification email when ipie-user-service publishes {@code
 * USER_REGISTRATION_COMPLETED} - see {@code
 * infrastructure.messaging.consumer.UserRegistrationCompletedEventConsumer} and {@code
 * application.service.NotificationService}.
 *
 * <p>{@code @EnableScheduling} drives {@code OutboxRelayScheduler} - the transactional outbox
 * relay (master standards doc, section 9) - not any business-specific scheduled job.
 */
@SpringBootApplication
@EnableScheduling
public class CommunicationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunicationServiceApplication.class, args);
    }
}
