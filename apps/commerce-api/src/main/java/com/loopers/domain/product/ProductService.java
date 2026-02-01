package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandService brandService;

    @Transactional
    public Product create(String name, int price, int stock, Long brandId) {
        Brand brand = brandService.getById(brandId);
        Product product = new Product(name, price, stock, brand);
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
