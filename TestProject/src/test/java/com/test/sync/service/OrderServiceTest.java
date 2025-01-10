package com.test.sync.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

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

	int threadCount = 100;

	long startTime;
	
	
	@BeforeEach
	void initProductstock() throws JsonProcessingException { 
		startTime = System.currentTimeMillis();
	}
	
	@AfterEach
	void printRunTime() {
		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
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
	
	/**
	 * 테스트 실행 (모든 테스트의 실행 메소드 외 기본 내용이 같으므로 공통 메소드로 추출) 
	 * @param testId 테스트 아이디 겸 상품 아이디
	 * @param function 실제 테스트를 진행할 메소드
	 * @return 주문 성공 개수
	 * @throws InterruptedException
	 * @throws JsonProcessingException 
	 */
	private String runTest(int testId, Function<Integer, Boolean> function) throws InterruptedException, JsonProcessingException {
		//상품 재고 초기화
		productService.updateProuctStock(testId, stock);
		
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		
		for (int i = 1; i <= threadCount; i++) {
			executorService.submit(() -> {
				boolean isSucceed = false;
				try {
					isSucceed = function.apply(testId); //테스트할 함수 실행
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
		return (String) redisTemplate.opsForHash().get(key, "succeedCount");
	}
	

	@Test
	@DisplayName("JPA Lock만 사용하는 주문 테스트 - 테스트 1번")
	void testOrderWithOnlyJPALock() throws InterruptedException, JsonProcessingException {
		int testId = 1;
		String succeedCount = runTest(testId,orderService::orderWithOnlyJPALock);
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

	@Test	
	@DisplayName("JPA Lock과 Redis(lock 없이) 사용하는 주문 테스트 - 테스트 2번")
	void testOrderWithJPALockAndRedis() throws InterruptedException, JsonProcessingException {
		int testId = 2;
		String succeedCount = runTest(testId, orderService::orderWithoutJPALockAndRedis);
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

	@Test
	@DisplayName("JPA Lock과 Redisson lock을 사용하는 주문 테스트 - 테스트 3번")
	void testOrderWithJPALockAndRedissonLock() throws InterruptedException, JsonProcessingException{
		int testId = 3;
		String succeedCount = runTest(testId, orderService::orderWithJPALockAndRedissonLock);
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

	@Test
	@DisplayName("JPA(lock x)와 Redisson lock을 사용하는 주문 테스트 - 테스트 4번")
	void testOrderWithOutJPALockAndRedissonLock() throws InterruptedException, JsonProcessingException {
		int testId = 4;
		String succeedCount = runTest(testId, orderService::orderWithoutJPALockAndRedissonLock);
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

	@Test
	@Disabled
	@DisplayName("jpa lock과 lettuce lock을 사용하는 주문 테스트 - 테스트 5번")
	void testOrderWithJPALockAndLettuceLock() throws InterruptedException, JsonProcessingException {
		int testId = 5; 
		String succeedCount = runTest(testId, orderService::orderWithJPALockAndLettuceLock);
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

	@Test
	@Disabled
	@DisplayName("jpa(lock x)와 lettuce lock을 사용하는 주문 테스트 - 테스트 6번")
	void testOrderWithOutJPALockAndLettuceLock() throws InterruptedException, JsonProcessingException {
		int testId = 6; 
		String succeedCount = runTest(testId, orderService::orderWithOutJPALockAndLettuceLock);
		assertEquals(Integer.parseInt(succeedCount), stock);
	}

}
