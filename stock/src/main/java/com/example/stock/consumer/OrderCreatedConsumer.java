package com.example.stock.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import com.example.order.domain.Order;
import com.example.stock.service.StockEventService;

@Component
public class OrderCreatedConsumer {

	Logger logger = LoggerFactory.getLogger(OrderCreatedConsumer.class);

	@Autowired
	StockEventService stockEventService;

	@SqsListener("qa-stockConsumerForOrderCreated")
	private void receiveMessage(final @NotificationMessage Order order) {
		logger.info("message received in queue qa-stockConsumerForOrderCreated {}", order);
		try {
			stockEventService.process(order);
		} catch (UnexpectedRollbackException e) {
			logger.info("Transaction rolled-back because there is not enough items of {} in stock to process this order",order);
		}
	}

}
