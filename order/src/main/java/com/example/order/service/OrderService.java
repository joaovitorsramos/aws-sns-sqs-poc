package com.example.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.order.domain.Order;
import com.example.order.domain.Status;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.repository.OrderItemRepository;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OrderService {

	Logger logger = LoggerFactory.getLogger(OrderService.class);

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	OrderItemRepository orderItemRepository;

	@Autowired
	NotificationMessagingTemplate messagingTemplate;

	public Order create(Order order) {
		order.setStatus(Status.APPROVAL_PENDING);
		order.getOrderItems().stream().forEach(i -> i.setItemStatus(Status.APPROVAL_PENDING));
		logger.info("saving record of {}", order);
		order = orderRepository.save(order);
		String orderJSON = null;
		try {
			orderJSON = new ObjectMapper().writeValueAsString(order);
		} catch (JsonProcessingException e) {
			logger.warn("Exception converting order object to JSON string. Exception: {} ", e);
		}
		logger.info("publishing message {} to AWS SNS", order);
		messagingTemplate.sendNotification("orderCreated", orderJSON, "Order Created");
		return order;
	}
	
	public Order findById(String id) {
		return orderRepository.findById(id).orElseThrow(() ->  new OrderNotFoundException(id));
	}


}
