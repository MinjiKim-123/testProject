package com.test.sync.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.test.sync.entity.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends CrudRepository<Product, Integer>{

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
