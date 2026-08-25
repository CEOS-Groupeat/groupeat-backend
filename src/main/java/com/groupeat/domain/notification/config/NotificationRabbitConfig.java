package com.groupeat.domain.notification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NotificationRabbitConfig {

    private final NotificationRabbitProperties properties;

    @Bean
    public MessageConverter notificationMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(properties.exchange(), true, false);
    }

    @Bean
    public Queue notificationFcmQueue() {
        return QueueBuilder.durable(properties.queue())
                .withArgument("x-dead-letter-exchange", properties.retryExchange())
                .withArgument("x-dead-letter-routing-key", properties.retryRoutingKey())
                .build();
    }

    @Bean
    public Binding notificationFcmBinding() {
        return BindingBuilder.bind(notificationFcmQueue())
                .to(notificationExchange())
                .with(properties.routingKey());
    }

    @Bean
    public DirectExchange notificationRetryExchange() {
        return new DirectExchange(properties.retryExchange(), true, false);
    }

    @Bean
    public Queue notificationFcmRetryQueue() {
        return QueueBuilder.durable(properties.retryQueue())
                .withArgument("x-message-ttl", properties.retryTtlMs())
                .withArgument("x-dead-letter-exchange", properties.exchange())
                .withArgument("x-dead-letter-routing-key", properties.routingKey())
                .build();
    }

    @Bean
    public Binding notificationFcmRetryBinding() {
        return BindingBuilder.bind(notificationFcmRetryQueue())
                .to(notificationRetryExchange())
                .with(properties.retryRoutingKey());
    }

    @Bean
    public DirectExchange notificationDeadLetterExchange() {
        return new DirectExchange(properties.deadLetterExchange(), true, false);
    }

    @Bean
    public Queue notificationFcmDeadLetterQueue() {
        return QueueBuilder.durable(properties.deadLetterQueue()).build();
    }

    @Bean
    public Binding notificationFcmDeadLetterBinding() {
        return BindingBuilder.bind(notificationFcmDeadLetterQueue())
                .to(notificationDeadLetterExchange())
                .with(properties.deadLetterRoutingKey());
    }
}
