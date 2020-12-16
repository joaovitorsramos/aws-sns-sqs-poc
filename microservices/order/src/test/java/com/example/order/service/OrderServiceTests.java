package com.example.order.service;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.order.domain.Order;
import com.example.order.domain.OrderItem;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.repository.OrderItemRepository;
import com.example.order.repository.OrderRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceTests {

	@Autowired
	OrderService orderService;

	@MockBean
	OrderRepository orderRepository;

	@MockBean
	OrderItemRepository orderItemRepository;

	@MockBean
	RabbitTemplate rabbitTemplate;

	private List<OrderItem> orderItemsList = new ArrayList<>();

	@BeforeEach
	public void createOrderItemsList() {
		orderItemsList.add(new OrderItem("123_aspirin", 100, "123", 100.00));
		orderItemsList.add(new OrderItem("456_ibuprofen", 200, "123", 100.00));

	}

	@Test
	public void whenInvalidIdOrderShouldNotBeFound() {
		Mockito.when(orderRepository.findById("AAA")).thenThrow(OrderNotFoundException.class);
		assertThrows(OrderNotFoundException.class, () -> orderService.findById("AAA"));

	}

	@Test
	public void whenValidIdOrderShouldBeFound() {
		Order order = Order.builder().orderId("123").walletId("123_peter").customerId("123_peter").build();
		Order mockOrderReturned = Order.builder().orderId("123").walletId("123_peter").customerId("123_peter").build();
		Mockito.when(orderRepository.findById("123")).thenReturn(Optional.of(mockOrderReturned));
		assertEquals(order, orderService.findById("123"));

	}


	

}
