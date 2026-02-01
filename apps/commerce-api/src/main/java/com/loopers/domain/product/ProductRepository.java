package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(Long id);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findAllByBrandId(Long brandId, Pageable pageable);

    void delete(Product product);

    void deleteAllByBrandId(Long brandId);
}
