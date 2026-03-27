package com.founderlink.notification_service.listener;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.founderlink.notification_service.config.RabbitMQConfig;

@Service
public class NotificationListener {

	private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

	@RabbitListener(
			queues = RabbitMQConfig.QUEUE_STARTUP,
			containerFactory = RabbitMQConfig.RABBIT_LISTENER_CONTAINER_FACTORY)
	public void onStartupCreated(Map<String, Object> payload) {
		logSimulatedEmail(
				"STARTUP CREATED",
				"startup.created",
				payload,
				"startupId",
				"founderId",
				"industry",
				"fundingGoal");
	}

	@RabbitListener(
			queues = RabbitMQConfig.QUEUE_INVESTMENT,
			containerFactory = RabbitMQConfig.RABBIT_LISTENER_CONTAINER_FACTORY)
	public void onInvestmentCreated(Map<String, Object> payload) {
		Object eventType = payload != null ? payload.get("eventType") : null;
		if ("INVESTMENT_STATUS_CHANGED".equals(eventType)) {
			logSimulatedEmail(
					"INVESTMENT STATUS UPDATED",
					"investment.status.*",
					payload,
					"investmentId",
					"startupId",
					"investorId",
					"status",
					"approvedByFounderId",
					"amount");
			return;
		}
		logSimulatedEmail(
				"INVESTMENT CREATED",
				"investment.created",
				payload,
				"investmentId",
				"startupId",
				"investorId",
				"amount");
	}

	@RabbitListener(
			queues = RabbitMQConfig.QUEUE_TEAM,
			containerFactory = RabbitMQConfig.RABBIT_LISTENER_CONTAINER_FACTORY)
	public void onTeamInvite(Map<String, Object> payload) {
		logSimulatedEmail(
				"TEAM INVITE",
				"team.invite",
				payload,
				"inviteId",
				"startupId",
				"invitedUserId",
				"role",
				"requesterId");
	}

	@RabbitListener(
			queues = RabbitMQConfig.QUEUE_MESSAGE,
			containerFactory = RabbitMQConfig.RABBIT_LISTENER_CONTAINER_FACTORY)
	public void onMessageSent(Map<String, Object> payload) {
		logSimulatedEmail(
				"NEW MESSAGE",
				"message.sent",
				payload,
				"messageId",
				"senderId",
				"receiverId",
				"content");
	}

	private void logSimulatedEmail(String title, String routingHint, Map<String, Object> payload, String... keys) {
		StringBuilder body = new StringBuilder();
		for (String key : keys) {
			Object v = payload != null ? payload.get(key) : null;
			if (v != null) {
				body.append(key).append(": ").append(v).append(System.lineSeparator());
			}
		}
		if (body.isEmpty() && payload != null && !payload.isEmpty()) {
			body.append(payload.entrySet().stream()
					.map(e -> e.getKey() + ": " + Objects.toString(e.getValue(), ""))
					.collect(Collectors.joining(System.lineSeparator())));
		}
		String raw = payload != null ? payload.toString() : "(null)";
		String bodyText = body.isEmpty() ? "(no known keys; see raw)" : body.toString().trim();
		log.info(
				"========== EMAIL (SIMULATED) ==========\n"
						+ "Subject: [{}]\n"
						+ "Routing hint: {}\n"
						+ "----------------------------------------\n"
						+ "{}\n"
						+ "Raw payload: {}\n"
						+ "========================================",
				title,
				routingHint,
				bodyText,
				raw);
	}
}
