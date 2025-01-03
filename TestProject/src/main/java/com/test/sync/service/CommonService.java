package com.test.sync.service;

import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class CommonService {
	
	private final RedisTemplate<String, String> stringRedisTemplate;
	
	private final ObjectMapper objectMapper;
	
	public <T> Optional<T> getRedisValue(@NotBlank String redisKey, @NotNull Class<T> classType) throws JsonProcessingException{
		String data = stringRedisTemplate.opsForValue().get(redisKey);
		if(data == null)
			return Optional.empty();
		
		return Optional.of(objectMapper.readValue(data, classType));
	}
	
	public void setRedisValue(@NotBlank String redisKey, @NotNull Object value) throws JsonProcessingException{
		String data = objectMapper.writeValueAsString(value);
		stringRedisTemplate.opsForValue().set(redisKey, data);
	}
	
}
