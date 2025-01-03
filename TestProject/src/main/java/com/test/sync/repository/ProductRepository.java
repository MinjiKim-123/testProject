package com.test.sync.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.test.sync.entity.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends CrudRepository<Product, Integer>{


	/**
	 * 비관적 락을 적용한 특정 제품 조회
	 * @param productId
	 * @param stock
	 * @return
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Product findById(int productId);
	
	/**
	 * 비관적 락을 적용한 특정 제품 조회
	 * @param productId
	 * @param stock
	 * @return
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Product findByIdAndStockGreaterThan(int productId, int stock);

	@Query("""
			select p
			from	Product p
			where	p.id=:productId
				and p.stock >:stock
			""")
	Product findByIdAndStockGreaterThanWithOutLock(int productId, int stock);
}
