package com.founderlink.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

	public static final String FOUNDERLINK_EXCHANGE = "founderlink-exchange";

	public static final String QUEUE_STARTUP = "startup-notification-queue";
	public static final String QUEUE_INVESTMENT = "investment-notification-queue";
	public static final String QUEUE_TEAM = "team-notification-queue";
	public static final String QUEUE_MESSAGE = "message-notification-queue";

	public static final String ROUTING_STARTUP_CREATED = "startup.created";
	public static final String ROUTING_INVESTMENT_CREATED = "investment.created";
	public static final String ROUTING_TEAM_INVITE = "team.invite";
	public static final String ROUTING_MESSAGE_SENT = "message.sent";

	public static final String RABBIT_LISTENER_CONTAINER_FACTORY = "rabbitListenerContainerFactory";

	@Bean
	public TopicExchange founderlinkExchange() {
		return new TopicExchange(FOUNDERLINK_EXCHANGE, true, false);
	}

	@Bean
	public Queue startupNotificationQueue() {
		return new Queue(QUEUE_STARTUP, true);
	}

	@Bean
	public Queue investmentNotificationQueue() {
		return new Queue(QUEUE_INVESTMENT, true);
	}

	@Bean
	public Queue teamNotificationQueue() {
		return new Queue(QUEUE_TEAM, true);
	}

	@Bean
	public Queue messageNotificationQueue() {
		return new Queue(QUEUE_MESSAGE, true);
	}

	@Bean
	public Binding startupNotificationBinding(Queue startupNotificationQueue, TopicExchange founderlinkExchange) {
		return BindingBuilder.bind(startupNotificationQueue).to(founderlinkExchange).with(ROUTING_STARTUP_CREATED);
	}

	@Bean
	public Binding investmentNotificationBinding(Queue investmentNotificationQueue, TopicExchange founderlinkExchange) {
		return BindingBuilder.bind(investmentNotificationQueue).to(founderlinkExchange).with(ROUTING_INVESTMENT_CREATED);
	}

	@Bean
	public Binding teamNotificationBinding(Queue teamNotificationQueue, TopicExchange founderlinkExchange) {
		return BindingBuilder.bind(teamNotificationQueue).to(founderlinkExchange).with(ROUTING_TEAM_INVITE);
	}

	@Bean
	public Binding messageNotificationBinding(Queue messageNotificationQueue, TopicExchange founderlinkExchange) {
		return BindingBuilder.bind(messageNotificationQueue).to(founderlinkExchange).with(ROUTING_MESSAGE_SENT);
	}

	@Bean
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	@Primary
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory,
			Jackson2JsonMessageConverter jackson2JsonMessageConverter,
			RabbitProperties rabbitProperties) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(jackson2JsonMessageConverter);
		factory.setAutoStartup(rabbitProperties.getListener().getSimple().isAutoStartup());
		return factory;
	}
}
