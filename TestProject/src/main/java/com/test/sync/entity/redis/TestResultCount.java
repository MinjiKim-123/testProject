package com.test.sync.entity.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@RedisHash(value = "TestResultCount")
public class TestResultCount {
	
	@Id
	private Integer testId;
	
	private Integer succeedCount;
	
	private Integer failedCount;
	
}
