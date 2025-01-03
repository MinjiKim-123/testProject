package com.test.sync.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.test.sync.SyncApplication;
import com.test.sync.entity.Product;
import com.test.sync.repository.ProductRepository;

@SpringBootTest(classes = SyncApplication.class)
class OrderServiceTest {
	
	@Autowired
	ProductService productService;
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	OrderService orderService;
	
  @BeforeEach
  @Transactional
  void initProductStock(){
  	productService.updateProuctStock(1, 100);
  }
  
	@Test
	void testOrderWithOnlyJPALock() throws InterruptedException {
		int productId = 1;
		
		int threadCount = 300;

    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 1; i <= threadCount; i++) {
        executorService.submit(() -> {
            try {
              orderService.orderWithOnlyJPALock(productId);
            }catch (Exception e) {
							System.out.println(e.getMessage());
						}  finally {
              latch.countDown();
            }
        });
    }

		latch.await();
		
		Product product = productRepository.findById(1);
		if (product == null)
			throw new NoSuchElementException("Failed to find product.");

    assertEquals(product.getStock(),0);
	}

	@Test
	void testOrderWithJPALockAndRedis() throws InterruptedException  {
		int productId = 1;
		
		int threadCount = 300;

    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 1; i <= threadCount; i++) {
        executorService.submit(() -> {
            try {
              orderService.orderWithOnlyJPALock(productId);
            }catch (Exception e) {
							System.out.println(e.getMessage());
						}  finally {
              latch.countDown();
            }
        });
    }

		latch.await();
		
		Product product = productRepository.findById(1);
		if(product == null)
  			throw new NoSuchElementException();
  			
    assertEquals(product.getStock(),0);
	}

	@Test
	@Disabled
	void testOrderWithJPALockAndRedissonLock() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled
	void testOrderWithJPALockAndLettuceLock() {
		fail("Not yet implemented");
	}

}
