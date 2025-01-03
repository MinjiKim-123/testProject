package com.test.sync.entity.redis;

import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductStock {

	private Integer id;
	
	private Integer stock;
	
	/**
	 * 재고 감소
	 */
	public void decreaseStock() {
		if(this.stock <= 0)
			throw new IllegalArgumentException("This product is out of stock.");
		
		this.stock--;
	}
	
	/**
	 * 재고 증가
	 */
	public void increaseStock() {
		this.stock++;
	}
}
