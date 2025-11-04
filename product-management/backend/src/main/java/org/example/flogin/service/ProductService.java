package org.example.flogin.service;

import org.example.flogin.dto.ProductCreateDTO;
import org.example.flogin.dto.ProductResponseDTO;
import org.example.flogin.dto.ProductUpdateDTO;
import org.example.flogin.entity.Product;
import org.example.flogin.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Helper method (Dùng để chuyển đổi Entity -> DTO)
    private ProductResponseDTO mapToDTO(Product product) {
        return new ProductResponseDTO(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            product.getCreatedAt()
        );
    }

    // a) Test createProduct()
    @Override
    public ProductResponseDTO createProduct(ProductCreateDTO createDTO) {
        Product product = new Product();
        // Ánh xạ DTO sang Entity
        product.setName(createDTO.name());
        product.setPrice(createDTO.price());
        product.setStock(createDTO.stock());
        // Mặc định tạo mới
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    // a) Test getProduct()
    @Override
    public Optional<ProductResponseDTO> getProduct(Long id) {
        return productRepository.findById(id).map(this::mapToDTO);
    }

    // a) Test updateProduct()
    @Override
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO updateDTO) {
        // FindById sẽ là điểm yếu cần TDD (Nếu không tìm thấy thì xử lý thế nào?)
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id)); // Placeholder Exception

        product.setName(updateDTO.name());
        product.setPrice(updateDTO.price());
        product.setStock(updateDTO.stock());

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    // a) Test deleteProduct()
    @Override
    public void deleteProduct(Long id) {
        // Tương tự, cần TDD xem có nên kiểm tra id tồn tại trước khi xóa không
        productRepository.deleteById(id);
    }

    // a) Test getAll() với pagination
    @Override
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponseDTO> content = productPage.getContent().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());

        // Trả về PageImpl để bọc lại thông tin phân trang (tổng số trang, tổng số phần tử,...)
        return new PageImpl<>(content, pageable, productPage.getTotalElements());
    }
}