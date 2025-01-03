package com.test.sync.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootTest
class ProductServiceTest {

	@Autowired
	ProductService productService;
	
	@Test
	void testInsertProuct() throws JsonProcessingException {
		int productId = productService.insertProuct("테스트 상품", 1000, 10);
		assertTrue(productId > 0);
	}

	@Test
	@Disabled
	void testProductService() {
		fail("Not yet implemented");
	}

}
