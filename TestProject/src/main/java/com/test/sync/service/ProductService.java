package com.test.sync.service;

import org.springframework.stereotype.Service;

import com.test.sync.entity.Product;
import com.test.sync.entity.ProductStock;
import com.test.sync.repository.ProductRepository;
import com.test.sync.repository.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	
	private final ProductStockRepository productStockRepository;
	
	
	public int insertProuct(String name, int price, int stock) {
		Product product = productRepository.save(Product.builder()
				.name(name)
				.price(price)
				.stock(stock)
				.build());
		//저장된 상품의 아이디
		int productId = product.getId();
		
		//재고 데이터를 redis에 저장
		ProductStock productStock = productStockRepository.save(ProductStock.builder()
				.id(productId)
				.stock(stock)
				.build());
		
		return productStock.getId();		
	}
}
