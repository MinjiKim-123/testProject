package com.test.sync.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.sync.entity.Product;
import com.test.sync.entity.redis.ProductStock;
import com.test.sync.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	
	private final CommonService  commonService;
	
	/**
	 * 상품 등록
	 * @param name 상품명
	 * @param price 가격
	 * @param stock 재고
	 * @return 등록한 상품 아이디
	 * @throws JsonProcessingException
	 */
	@Transactional
	public int insertProuct(String name, int price, int stock) throws JsonProcessingException {
		Product product = productRepository.save(Product.builder()
				.name(name)
				.price(price)
				.stock(stock)
				.build());
		//저장된 상품의 아이디
		int productId = product.getId();
		
		//재고 데이터를 redis에 저장	
		this.saveProductStockToRedis(productId, stock);

		return productId;
	}
	
	/**
	 * 상품 재고 수정
	 * @param productId 상품아이디
	 * @param stock 재고
	 * @throws JsonProcessingException
	 */
	@Transactional
	public void updateProuctStock(int productId, int stock) throws JsonProcessingException {		
		Product product = productRepository.findById(productId);
		if (product == null) {
			product = Product.builder()
					.id(productId)
					.build();
		}
  	product.setStock(stock);
  	saveProductStockToRedis(productId, stock); //redis에도 해당 데이터 반영
	}

	
	/**
	 * redis에서 상품 재고 데이터 조회
	 * @param productId 상품 아이디
	 * @return redis에 저장된 상품 재고 데이터
	 * @throws JsonProcessingException
	 */
	public Optional<ProductStock> findProductStockToRedis(int productId) throws JsonProcessingException {
		String redisKey = "productStock::"+productId;
		return commonService.getRedisValue(redisKey, ProductStock.class);
	}
	
	/**
	 * redis에 상품 재고 데이터 저장 
	 * @param productId 상품 아이디
	 * @param stock 재고
	 * @return 상품 재고 데이터
	 * @throws JsonProcessingException
	 */
	public ProductStock saveProductStockToRedis(int productId, int stock) throws JsonProcessingException {
		String redisKey = "productStock::"+productId;
		ProductStock data = ProductStock.builder()
				.id(productId)
				.stock(stock)
				.build();
		commonService.setRedisValue(redisKey, data);
		return data;
	}
}
