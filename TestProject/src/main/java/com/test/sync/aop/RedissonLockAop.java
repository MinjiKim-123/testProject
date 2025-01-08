package com.test.sync.aop;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import com.test.sync.util.RedisLockKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Redisson annotation용 AOP
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
@Component
public class RedissonLockAop {

	private final RedissonClient redissonClient;
	
	@Around("@annotation(com.test.sync.aop.RedissonLock)")
	public Object aroundLock(final ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature(); //메소드의 리턴,파라미터,이름 정보
    Method method = methodSignature.getMethod();
    RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
    
    //lock 키 생성
    String lockKey = RedisLockKeyGenerator.generate(methodSignature.getName(), methodSignature.getParameterNames(), joinPoint.getArgs(), redissonLock.keyName());
    RLock lock = redissonClient.getLock(lockKey);
    
    try {
			boolean isSucceed = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), redissonLock.timeUnit());
			log.info("Redisson lock get tried. Result : " + isSucceed);
			if(!isSucceed) {
				log.info("trylock failed");
				return false;
			}
			
			return joinPoint.proceed();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			throw e;
		}finally {
			try {
				lock.unlock();
				log.info("Redisson unlocked.");
			}catch (Exception e) {
				log.error("Redisson unlock failed.", e);
			}
		}
    
	}
}
