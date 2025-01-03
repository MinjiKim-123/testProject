package com.test.sync.redis.dto;

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
	
	public void decreaseStock() {
		if(this.stock <= 0)
			throw new IllegalArgumentException("This product is out of stock.");
		
		this.stock--;
	}
	
	public void increaseStock() {
		this.stock++;
	}
}
