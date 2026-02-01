package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product create(String name, int price, int stock, Long brandId, String brandName) {
        Product product = new Product(name, price, stock, brandId, brandName);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
    }

    @Transactional(readOnly = true)
    public Page<Product> getAll(Long brandId, Pageable pageable) {
        if (brandId != null) {
            return productRepository.findAllByBrandId(brandId, pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> findAllByBrandId(Long brandId) {
        return productRepository.findAllByBrandId(brandId);
    }

    @Transactional
    public Product update(Long id, String name, int price, int stock) {
        Product product = getById(id);
        product.update(name, price, stock);
        return product;
    }

    @Transactional
    public void delete(Long id) {
        Product product = getById(id);
        productRepository.delete(product);
    }

    @Transactional
    public void deleteByBrandId(Long brandId) {
        productRepository.deleteAllByBrandId(brandId);
    }
}
