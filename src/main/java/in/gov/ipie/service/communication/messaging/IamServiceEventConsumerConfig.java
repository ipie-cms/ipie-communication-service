package in.gov.ipie.service.communication.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.gov.ipie.common.events.deadletter.DeadLetterSupport;

/**
 * Binds this service to <b>ipie-iam-service's</b> exchange - the second publisher it listens to,
 * alongside ipie-user-service's (see {@link UserServiceEventConsumerConfig}).
 *
 * <p>It exists because the set-password link comes from iam, not from user-service. iam owns
 * credentials, so it mints the one-time token and asks for it to be mailed directly, rather than
 * routing a credential-setting secret through a third service that has no business holding one.
 *
 * <p>Declaring the other service's exchange here is safe and intentional: an AMQP exchange
 * declaration is idempotent, and whichever side starts first creates it. Without it, a comms service
 * that boots before ipie-iam-service has ever run would have nothing to bind its queue to.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
public class IamServiceEventConsumerConfig {

    @Bean
    public TopicExchange iamServiceEventsExchange(
            @Value("${ipie.integrations.iam-service.rabbitmq.exchange:ipie-iam-service.events}") String exchange) {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue credentialSetupRequestedQueue(
            @Value("${ipie.integrations.iam-service.rabbitmq.credential-setup-queue:"
                    + "ipie-communication-service.events.credential-setup-requested}") String queue) {
        return DeadLetterSupport.workQueue(queue);
    }

    @Bean
    public Binding credentialSetupRequestedBinding(
            Queue credentialSetupRequestedQueue, TopicExchange iamServiceEventsExchange) {
        return BindingBuilder.bind(credentialSetupRequestedQueue)
                .to(iamServiceEventsExchange)
                .with("ACCOUNT_CREDENTIAL_SETUP_REQUESTED");
    }
}
