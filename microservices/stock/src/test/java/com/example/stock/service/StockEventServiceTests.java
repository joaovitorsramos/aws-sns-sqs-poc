package com.example.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.order.domain.OrderItem;
import com.example.stock.domain.Stock;
import com.example.stock.domain.StockEvent;
import com.example.stock.repository.StockEventRepository;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StockEventServiceTests {

	@Autowired
	StockEventService stockEventService;

	@MockBean
	StockEventRepository stockEventRepository;

	@MockBean
	StockService stockService;

	@MockBean
	RabbitTemplate rabbitTemplate;
	
	public List<OrderItem> orderItemsList = new ArrayList<>();

	@BeforeEach
	public void createOrderItemsList() {
		orderItemsList.add(OrderItem.builder().sku("123_aspirin").amount(100).branchId("123").cost(100.00).build());
		orderItemsList.add(OrderItem.builder().sku("456_ibuprofen").amount(100).branchId("123").cost(100.00).build());

	}

	@Test
	public void whenSaveStockEventReturnUpdatedStock() {
		StockEvent stockEvent = StockEvent.builder()
									.sku("123_aspirin")
									.amount(1)
									.branchId("123")
									.build();
		StockEvent mockStockEventReturned = stockEvent.toBuilder().build();
		
		// Assuming stock already have 100 items of "123_aspirin" SKU
		Stock mockStockReturned = new Stock(stockEvent.getSku(), stockEvent.getAmount() + 100, stockEvent.getBranchId());
		Stock expectedStockReturned = mockStockReturned.toBuilder().build();
		
		Mockito.when(stockEventRepository.save(stockEvent)).thenReturn(mockStockEventReturned);
		Mockito.when(stockService.save(any())).thenReturn(mockStockReturned);
		assertEquals(expectedStockReturned, stockEventService.save(stockEvent));
	}

	

}
