package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional
    public Brand create(String name) {
        Brand brand = new Brand(name);
        return brandRepository.save(brand);
    }

    @Transactional(readOnly = true)
    public Brand getById(Long id) {
        return brandRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 브랜드입니다."));
    }

    @Transactional(readOnly = true)
    public Page<Brand> getAll(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    @Transactional
    public Brand update(Long id, String name) {
        Brand brand = getById(id);
        brand.update(name);
        return brand;
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = getById(id);
        brandRepository.delete(brand);
    }
}
