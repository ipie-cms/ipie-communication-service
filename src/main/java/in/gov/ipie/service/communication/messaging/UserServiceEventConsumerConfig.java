package in.gov.ipie.service.communication.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import in.gov.ipie.common.events.deadletter.DeadLetterSupport;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cross-service RabbitMQ wiring for {@link
 * in.gov.ipie.service.communication.messaging.UserRegistrationCompletedEventConsumer}
 * - binds to ipie-user-service's own exchange (not this service's, unlike the template's
 * self-consumption {@code RabbitConsumerConfig}/{@code RabbitUserEventLogConsumer}). Reuses {@code
 * RabbitConsumerConfig}'s {@code rabbitListenerContainerFactory} bean (not redeclared here) -
 * {@code @RabbitListener} needs exactly one factory in the context, and {@code @EnableRabbit} is
 * already active via that class.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
public class UserServiceEventConsumerConfig {

    @Bean
    public TopicExchange userServiceEventsExchange(
            @Value("${ipie.integrations.user-service.rabbitmq.exchange:ipie-user-service.events}") String exchange) {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue userRegistrationCompletedQueue(
            @Value("${ipie.integrations.user-service.rabbitmq.registration-completed-queue:"
                    + "ipie-communication-service.events.registration-completed}") String queue) {
        return DeadLetterSupport.workQueue(queue);
    }

    @Bean
    public Binding userRegistrationCompletedBinding(Queue userRegistrationCompletedQueue, TopicExchange userServiceEventsExchange) {
        return BindingBuilder.bind(userRegistrationCompletedQueue).to(userServiceEventsExchange).with("USER_REGISTRATION_COMPLETED");
    }

    @Bean
    public Queue registrationEmailOtpQueue(
            @Value("${ipie.integrations.user-service.rabbitmq.registration-email-otp-queue:"
                    + "ipie-communication-service.events.registration-email-otp}") String queue) {
        return DeadLetterSupport.workQueue(queue);
    }

    @Bean
    public Binding registrationEmailOtpBinding(Queue registrationEmailOtpQueue, TopicExchange userServiceEventsExchange) {
        return BindingBuilder.bind(registrationEmailOtpQueue).to(userServiceEventsExchange).with("REGISTRATION_EMAIL_OTP_REQUESTED");
    }

    @Bean
    public Queue userLoggedInQueue(
            @Value("${ipie.integrations.user-service.rabbitmq.logged-in-queue:"
                    + "ipie-communication-service.events.logged-in}") String queue) {
        return DeadLetterSupport.workQueue(queue);
    }

    @Bean
    public Binding userLoggedInBinding(Queue userLoggedInQueue, TopicExchange userServiceEventsExchange) {
        return BindingBuilder.bind(userLoggedInQueue).to(userServiceEventsExchange).with("USER_LOGGED_IN");
    }
}
