package com.test.sync.util;

import lombok.experimental.UtilityClass;

/**
 * redisson lock key 생성을 위한 유틸리티 클래스
 */
@UtilityClass
public class RedissonLockKeyGenerator {

	private static final String LOCK_KEY_PREFIX = "LOCK_";
	
	/**
	 * 생성
	 * @param methodName
	 * @param parameterNames
	 * @param args
	 * @param keyName
	 * @return
	 */
	public String generate(String methodName, String[] parameterNames, Object[] args, String keyName) {
		
		Object keyValue = null;
		
    for (int i = 0; i < parameterNames.length; i++) {
        if (parameterNames[i].equals(keyName)) {
        		keyValue = args[i];
            break;
        }
    }

    if(keyValue == null)
    	throw new IllegalArgumentException("Failed to find key value.");
    	
    return LOCK_KEY_PREFIX + keyName + ":" + keyValue;
	}
}
