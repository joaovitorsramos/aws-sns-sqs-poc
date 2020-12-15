package com.example.stock.consumer;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationSubject;
import org.springframework.cloud.aws.messaging.endpoint.NotificationStatus;
import org.springframework.cloud.aws.messaging.endpoint.annotation.NotificationMessageMapping;
import org.springframework.cloud.aws.messaging.endpoint.annotation.NotificationSubscriptionMapping;
import org.springframework.cloud.aws.messaging.endpoint.annotation.NotificationUnsubscribeConfirmationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.order.domain.Order;
import com.example.stock.service.StockEventService;
import com.example.stock.service.StockService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/events/orderCreated")
public class OrderCreatedSubscriber {
	Logger logger = LoggerFactory.getLogger(OrderCreatedSubscriber.class);

	@Autowired
	StockService stockService;

	@Autowired
	StockEventService stockEventService;
	
	@NotificationSubscriptionMapping
	public void handleSubscriptionMessage(NotificationStatus status) throws IOException {
		//We subscribe to start receive the message
		status.confirmSubscription();
	}
	
	@NotificationUnsubscribeConfirmationMapping
	public void handleUnsubscribeMessage(NotificationStatus status) {
		//e.g. the client has been unsubscribed and we want to "re-subscribe"
		status.confirmSubscription();
	}
	
    @NotificationMessageMapping
    public void receiveNotification(@NotificationMessage String message, @NotificationSubject String subject) {
    	logger.info("message received:  {}", message);
    	
    	Order order = null;
		try {
			order = new ObjectMapper().readValue(message, Order.class);
		} catch (JsonProcessingException e1) {
			logger.warn("Exception converting message to order object. Exception: {} ", e1);
		}
		logger.info("OrderCreated:  {}", order);	
		
    	try {
			stockEventService.process(order);
		} catch (UnexpectedRollbackException e) {
			logger.info("Transaction rolled-back because there is not enough items of {} in stock to process this order",order);
		}
	
    }
		
    


}

