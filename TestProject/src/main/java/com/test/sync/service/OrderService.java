package com.test.sync.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.sync.aop.LettuceLock;
import com.test.sync.aop.RedissonLock;
import com.test.sync.entity.Product;
import com.test.sync.entity.redis.ProductStock;
import com.test.sync.repository.ProductRepository;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class OrderService {

	private final ProductRepository productRepository;

	private final ProductService productService;

	/**
	 * rdb에 저장된 상품의 재고를 jpa lock을 사용해서 재고 차감
	 * 
	 * @param productId 상품 아이디
	 */
	private void decreaseStockJPALock(int productId) {
		Product product = productRepository.findById(productId);
		if (product == null)
			throw new NoSuchElementException("Failed to find product.");
		product.decreaseStock();// 재고 차감
	}

	/**
	 * rdb에 저장된 상품의 재고를 jpa lock없이 조회해서 재고 차감
	 * 
	 * @param productId 상품 아이디
	 */
	private void decreaseStockWithoutJpaLock(int productId) {
		Product product = productRepository.findByIdWithoutLock(productId);
		if (product == null)
			throw new NoSuchElementException("Failed to find product.");
		product.decreaseStock();// 재고 차감
	}

	/**
	 * redis에 저장된 상품 재고 정보 조회해서 차감
	 * 
	 * @param productId 상품 아이디
	 * @throws JsonProcessingException
	 */
	private void decreaseStockRedis(int productId) throws JsonProcessingException {
		Optional<ProductStock> stockOpt = productService.findProductStockToRedis(productId);
		ProductStock productStock;

		if (stockOpt.isEmpty()) { // redis에 해당 제품의 재고 정보가 없을 경우
			Product product = productRepository.findById(productId); // rdb에서 제품 정보 조회
			if (product == null)
				throw new NoSuchElementException("Failed to find product.");

			int stock = product.getStock();
			productStock = productService.saveProductStockToRedis(productId, stock); // redis에서 조회 가능하도록 rdb에 있는 데이터를 redis에
																																								// 저장
		} else
			productStock = stockOpt.get(); // redis에 저장된 재고 정보가 있을 경우

		// redis 상품 재고 감소
		productStock.decreaseStock();
		productService.saveProductStockToRedis(productId, productStock.getStock());
	}

	/**
	 * jpa lock만 사용하는 주문
	 * 
	 * @return
	 */
	@Transactional
	public Boolean orderWithOnlyJPALock(@Min(value = 1, message = "Product id must be greater than 1.") int productId) {
		boolean result = false;
		try {
			decreaseStockJPALock(productId);
		} catch (Exception e) {
			System.out.println("Failed order with jpa lock." + e.getMessage());
		}
		return result;
	}

	/**
	 * jpa lock과 redis를 사용하는 주문
	 * 
	 * @throws JsonProcessingException
	 */
	@Transactional
	public Boolean orderWithJPALockAndRedis(@Min(value = 1, message = "Product id must be greater than 1.") int productId) {
		boolean result = false;
		try {
			decreaseStockRedis(productId);
			decreaseStockJPALock(productId);
			result = true;
		} catch (Exception e) {
			System.out.println("Failed order with jpa lock and redis." + e.getMessage());
		}
		return result;
	}

	/**
	 * jpa(lock x)과 redis를 사용하는 주문
	 * 
	 * @throws JsonProcessingException
	 */
	@Transactional
	public Boolean orderWithoutJPALockAndRedis(@Min(value = 1, message = "Product id must be greater than 1.") int productId) {
		boolean result = false;
		try {
			decreaseStockRedis(productId);
			decreaseStockWithoutJpaLock(productId);
		} catch (Exception e) {
			System.out.println("Failed order with redis." + e.getMessage());
		}
		return result;
	}

	/**
	 * jpa lock과 redisson lock을 사용하는 주문
	 * 
	 * @throws JsonProcessingException
	 */
	@Transactional
	@RedissonLock(keyName = "productId")
	public Boolean orderWithJPALockAndRedissonLock(@Min(value = 1, message = "Product id must be greater than 1.") int productId){
		boolean result = false;
		try {
			decreaseStockRedis(productId);
			decreaseStockJPALock(productId);
		} catch (Exception e) {
			System.out.println("Failed order with jpa lock and redis." + e.getMessage());
		}
		return result;
	}

	/**
	 * jpa(lock x)과 redisson lock을 사용하는 주문
	 * 
	 * @throws JsonProcessingException
	 */
	@Transactional
	@RedissonLock(keyName = "productId")
	public Boolean orderWithoutJPALockAndRedissonLock(
			@Min(value = 1, message = "Product id must be greater than 1.") int productId){
		boolean result = false;
		try {
			decreaseStockRedis(productId);
			decreaseStockWithoutJpaLock(productId);
		} catch (Exception e) {
			System.out.println("Failed order with redisson lock." + e.getMessage());
		}
		return result;
	}

	/**
	 * jpa lock과 lettuce lock을 사용하는 주문
	 * 
	 * @throws JsonProcessingException
	 */
	@Transactional
	@LettuceLock(keyName = "productId")
	public Boolean orderWithJPALockAndLettuceLock(@Min(value = 1, message = "Product id must be greater than 1.") int productId){
		boolean result = false;
		try {
			decreaseStockRedis(productId);
			decreaseStockJPALock(productId);
		} catch (Exception e) {
			System.out.println("Failed order with jpa lock and lettuce lock." + e.getMessage());
		}
		return result;
	}

	/**
	 * jpa(lock x)와 lettuce lock을 사용하는 주문
	 * 
	 * @throws JsonProcessingException
	 */
	@Transactional
	@LettuceLock(keyName = "productId")
	public Boolean orderWithOutJPALockAndLettuceLock(@Min(value = 1, message = "Product id must be greater than 1.") int productId) {
		boolean result = false;
		try {
			decreaseStockRedis(productId);
			decreaseStockWithoutJpaLock(productId);
		} catch (Exception e) {
			System.out.println("Failed order with lettuce lock." + e.getMessage());
		}
		return result;
	}

}
