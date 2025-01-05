package com.test.sync.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 분산락 적용을 위한 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonLock {

	/**
	 * 락 Key에 사용할 값의 이름
	 */
	String keyName();

	/**
	 * lock 시간 단위
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;

	/**
	 * lock 획득을 위한 최대 wait 시간
	 */
	long waitTime() default 10l;

	/**
	 * 해당 시간만큼 lock 임대
	 */
	long leaseTime() default 5l;
}
