package org.example.flogin.service;

import org.example.flogin.dto.ProductCreateDTO;
import org.example.flogin.dto.ProductResponseDTO;
import org.example.flogin.dto.ProductUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IProductService {
    ProductResponseDTO createProduct(ProductCreateDTO createDTO);
    Optional<ProductResponseDTO> getProduct(Long id);
    ProductResponseDTO updateProduct(Long id, ProductUpdateDTO updateDTO);
    void deleteProduct(Long id);
    Page<ProductResponseDTO> getAllProducts(Pageable pageable);
}
