package com.test.sync.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.sync.SyncApplication;

@SpringBootTest(classes = SyncApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderServiceTest {

	@Autowired
	ProductService productService;

	@Autowired
	OrderService orderService;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	int productId = 1;

	int stock = 10;

	int threadCount = 15;

	long startTime;
	
	/**
	 * rdb 상품 재고 초기화
	 */
	@BeforeEach
	void initProductstock() throws JsonProcessingException { 
		productService.updateProuctStock(productId, stock);
		startTime = System.currentTimeMillis();
	}
	
	@AfterEach
	void printRunTime() {
		long endTime = System.currentTimeMillis();
		long runTime = startTime - endTime;
		System.out.println("실행시간 : " + runTime);
	}
	
	/**
	 * redis의 테스트 결과 데이터 초기화
	 */
	@BeforeAll
	void initTestReulstCount() {
		Set<String>keys = redisTemplate.keys("TestResultCount:*");
		for(String key : keys) {
			redisTemplate.opsForHash().putAll(key, Map.of("succeedCount", "0", "failedCount", "0"));
		}
	}

	/**
	 * 성공,실패 여부 카운트 업데이트
	 * @param testId
	 * @param isSucceed
	 */
	void updateOrderHisCount(int testId, boolean isSucceed) {
		String key = "TestResultCount:" + testId;
		String columnName = isSucceed ? "succeedCount" : "failedCount";
		redisTemplate.opsForHash().increment(key, columnName, 1);
	}
	
	@Test
@Disabled
	@DisplayName("JPA Lock만 사용하는 주문 테스트 - 테스트 1번")
	void testOrderWithOnlyJPALock() throws InterruptedException {
		int testId = 1;

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 1; i <= threadCount; i++) {
			executorService.submit(() -> {
				boolean isSucceed = false;
				try {
					orderService.orderWithOnlyJPALock(productId);
					isSucceed = true;
				} catch (Exception e) {
					System.out.println(e.getMessage());
				} finally {
					updateOrderHisCount(testId, isSucceed);
					latch.countDown();
				}
			});
		}

		latch.await();

		String key = "TestResultCount:"+testId;
		String succeedCount = (String) redisTemplate.opsForHash().get(key, "succeedCount");
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

	@Test
	@Disabled
	@DisplayName("JPA Lock과 Redis(lock 없이) 사용하는 주문 테스트 - 테스트 2번")
	void testOrderWithJPALockAndRedis() throws InterruptedException {
		int testId = 2;

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 1; i <= threadCount; i++) {
			executorService.submit(() -> {
				boolean isSucceed = false;
				try {
					orderService.orderWithJPALockAndRedis(productId);
					isSucceed = true;
				} catch (Exception e) {
					System.out.println(e.getMessage());
				} finally {
					updateOrderHisCount(testId, isSucceed);
					latch.countDown();
				}
			});
		}

		latch.await();
		
		String key = "TestResultCount:" + testId;
		String succeedCount = (String) redisTemplate.opsForHash().get(key, "succeedCount");
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

	@Test
	@DisplayName("JPA Lock과 Redisson lock을 사용하는 주문 테스트 - 테스트 3번")
	void testOrderWithJPALockAndRedissonLock() throws InterruptedException {
		int testId = 3;

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 1; i <= threadCount; i++) {
			executorService.submit(() -> {
				boolean isSucceed = false;
				try {
					orderService.orderWithJPALockAndRedissonLock(productId);
					isSucceed = true;
				} catch (Exception e) {
					System.out.println(e.getMessage());
				} finally {
					updateOrderHisCount(testId, isSucceed);
					latch.countDown();
				}
			});
		}

		latch.await();

		String key = "TestResultCount:" + testId;
		String succeedCount = (String) redisTemplate.opsForHash().get(key, "succeedCount");
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

	@Test
	@Disabled
	void testOrderWithJPALockAndLettuceLock() {
		fail("Not yet implemented");
	}

}
