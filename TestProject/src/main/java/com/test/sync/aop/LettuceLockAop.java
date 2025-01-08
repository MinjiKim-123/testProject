package com.test.sync.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.test.sync.util.RedisLockKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LettuceLock annotation용 AOP
 */
@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class LettuceLockAop {

	private final RedisTemplate<String, String> redisTemplate;

	@Around("@annotation(com.test.sync.aop.LettuceLock)")
	public Object aroundLock(final ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature(); // 메소드의 리턴,파라미터,이름 정보
		Method method = methodSignature.getMethod();
		LettuceLock lettuceLock = method.getAnnotation(LettuceLock.class);

		// lock 키 생성
		String lockKey = RedisLockKeyGenerator.generate(methodSignature.getName(), methodSignature.getParameterNames(),
				joinPoint.getArgs(), lettuceLock.keyName());

		try {
			int maxTryTime = 10; //최대 시도 횟수를 10번으로 지정
			boolean isGetLock = false;
			while (!isGetLock && maxTryTime > 0) { // lock을 얻을때까지 실행
				isGetLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", lettuceLock.timeout(),
						lettuceLock.timeUnit()); // 값이 없으면 set하는 setnx 명령어 사용
				maxTryTime--;
			}
			
			return joinPoint.proceed();
		} finally {
			redisTemplate.delete(lockKey); // unlock처리
			log.info("Lettuce lock unlocked.");
		}
	}

}
