package com.test.sync.aop;

import java.util.concurrent.TimeUnit;

/**
 * lettuce를 사용한 분산락 적용을 위한 어노테이션
 */
public @interface LettuceLock {

	/**
	 * 락 Key에 사용할 값의 이름
	 */
	String keyName();
	
	/**
	 * lock 유효 시간의 단위
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;

	
	/**
	 * lock 유효 시간
	 */
	long timeout() default 5l;
}
