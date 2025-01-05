package com.test.sync.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.sync.SyncApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = SyncApplication.class)
@Slf4j
class OrderServiceTest {

	@Autowired
	ProductService productService;

	@Autowired
	OrderService orderService;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	private static final int PRODUCT_ID = 1;

	private static final int STOCK = 10;

	private static final int THREAD_COUNT = 15;

	private static final Map<String, Object> TEST_RESULT_COUNT_INIT_MAP = Map.of("succeedCount", "0", "failedCount", "0");

	@BeforeEach
	void initProductStock() throws JsonProcessingException { 
		productService.updateProuctStock(PRODUCT_ID, STOCK);
	}

	@Test
	@DisplayName("JPA Lock만 사용하는 주문 테스트 - 테스트 1번")
	void testOrderWithOnlyJPALock() throws InterruptedException {
		int testId = 1;
		String key = "TestResultCount:"+testId;
		redisTemplate.opsForHash().putAll(key, TEST_RESULT_COUNT_INIT_MAP);

		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

		for (int i = 1; i <= THREAD_COUNT; i++) {
			executorService.submit(() -> {
				boolean isSucceed = false;
				try {
					orderService.orderWithOnlyJPALock(PRODUCT_ID);
					isSucceed = true;
				} catch (Exception e) {
					log.error(e.getMessage());
				} finally {
					updateOrderHisCount(testId, isSucceed);
					latch.countDown();
				}
			});
		}

		latch.await();

		String succeedCount = (String) redisTemplate.opsForHash().get(key, "succeedCount");
		assertEquals(Integer.parseInt(succeedCount), STOCK);
	}

	void updateOrderHisCount(int testId, boolean isSucceed) {
		String key = "TestResultCount:" + testId;
		String columnName = isSucceed ? "succeedCount" : "failedCount";
		redisTemplate.opsForHash().increment(key, columnName, 1);
	}

	@Test
	@DisplayName("JPA Lock과 Redis(lock 없이) 사용하는 주문 테스트 - 테스트 2번")
	void testOrderWithJPALockAndRedis() throws InterruptedException {
		int testId = 2;
		String key = "TestResultCount:" + testId;
		redisTemplate.opsForHash().putAll(key, TEST_RESULT_COUNT_INIT_MAP);

		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

		for (int i = 1; i <= THREAD_COUNT; i++) {
			executorService.submit(() -> {
				boolean isSucceed = false;
				try {
					orderService.orderWithJPALockAndRedis(PRODUCT_ID);
					isSucceed = true;
				} catch (Exception e) {
					log.error(e.getMessage());
				} finally {
					updateOrderHisCount(testId, isSucceed);
					latch.countDown();
				}
			});
		}

		latch.await();

		String succeedCount = (String) redisTemplate.opsForHash().get(key, "succeedCount");
		assertEquals(Integer.parseInt(succeedCount), STOCK);
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
