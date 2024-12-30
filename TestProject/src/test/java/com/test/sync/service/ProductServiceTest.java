package com.test.sync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductServiceTest {

	@Autowired
	ProductService productService;
	
	@Test
	void testInsertProuct() {
		int productId = productService.insertProuct("테스트 상품1", 1000, 10);
		assertThat(productId).isGreaterThan(0);
	}

	@Test
	void testProductService() {
		fail("Not yet implemented");
	}

}
