package com.test.sync.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.sync.entity.Product;
import com.test.sync.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final ProductRepository productRepository;
	
	/**
	 * jpa lock만 사용하는 주문
	 * @return
	 */
	@Transactional
	public void orderWithOnlyJPALock(int productId) {
		Product product = productRepository.findByIdAndStockGreaterThan(productId, 0);
		if(product == null)
			throw new NoSuchElementException("Failed to find product.");
		int originStock = product.getStock();
		product.setStock(originStock - 1);
	}
	
	/**
	 * jpa lock과 redis를 사용하는 주문
	 */
	@Transactional
	public void orderWithJPALockAndRedis(int productId) {
	}
	
	/**
	 * redis만 사용하는 주문
	 */
	@Transactional
	public void orderWithRedis(int productId) {
	}
}
