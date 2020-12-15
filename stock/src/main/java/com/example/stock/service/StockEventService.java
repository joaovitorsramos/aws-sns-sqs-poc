package com.example.stock.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.order.domain.Order;
import com.example.stock.domain.Stock;
import com.example.stock.domain.StockEvent;
import com.example.stock.domain.StockMessage;
import com.example.stock.exceptions.OutOfStockException;
import com.example.stock.repository.StockEventRepository;

@Service
public class StockEventService {

	Logger logger = LoggerFactory.getLogger(StockEventService.class);


	@Autowired
	StockService stockService;

	@Autowired
	StockEventRepository stockEventRepository;

	@Transactional(propagation = Propagation.REQUIRED)
	public Stock save(StockEvent stockEvent) {
		logger.info("saving record {}", stockEvent);
		stockEvent = stockEventRepository.save(stockEvent);
		Stock stock = new Stock(stockEvent.getSku(), stockEvent.getAmount(), stockEvent.getBranchId());
		logger.info("calling stockService.save passing {}", stockEvent);
		stock = stockService.save(stock);
		return stock;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized void process(Order order) {
		var stockMessageSuccessList = new ArrayList<StockMessage>();
		var stockMessageFailList = new ArrayList<StockMessage>();
		order.getOrderItems().stream().forEach((s) -> {
			var stockMessage = new StockMessage(s.getSku(), s.getAmount(), s.getBranchId(), order.getOrderId(),
									s.getOrderItemId());
			try {
				var stockEvent = new StockEvent(s.getSku(), -s.getAmount(), s.getBranchId());
				logger.info("saving record {}", stockEvent);
				stockEventRepository.save(stockEvent);
				var stock = new Stock(s.getSku(), -s.getAmount(), s.getBranchId());
				logger.info("calling stockService.save passing {}", stockEvent);
				stock = stockService.save(stock);
				stockMessageSuccessList.add(new StockMessage(stock.getSku(), stock.getAmount(), stock.getBranchId(),
						order.getOrderId(), s.getOrderItemId()));
			} catch (OutOfStockException e) {
				logger.info(e.getMessage());
				stockMessageFailList.add(stockMessage);
			}
		});
		if (!stockMessageFailList.isEmpty()) {
			logger.info("Out of Stock: {}", stockMessageFailList);

		} else if (!stockMessageSuccessList.isEmpty()) {
			logger.info("Stock succesfully update: {}", stockMessageSuccessList);
		}
	}

}
