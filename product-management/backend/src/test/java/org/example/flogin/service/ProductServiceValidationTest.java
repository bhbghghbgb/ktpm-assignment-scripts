package org.example.flogin.service;

import org.example.flogin.dto.ProductCreateDTO;
import org.example.flogin.dto.ProductResponseDTO;
import org.example.flogin.dto.ProductUpdateDTO;
import org.example.flogin.entity.Product;
import org.example.flogin.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("RED: ProductService Validation Unit Tests")
public class ProductServiceValidationTest {

    // Dữ liệu mẫu (Sample Data)
    private final Product sampleProduct = createSampleProduct(1L, "Laptop", 1200.0, 10);
    private final ProductCreateDTO createDTO = new ProductCreateDTO("PC Gaming", 2500.0, 5);
    private final ProductUpdateDTO updateDTO = new ProductUpdateDTO("Laptop Pro", 1500.0, 8);
    @Mock
    private ProductRepository productRepository; // Không dùng nhưng cần InjectMocks
    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Product createSampleProduct(Long id, String name, Double price, Integer stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        return product;
    }

    // --- Tests cho CRUD (TDD RED) ---

    @Test
    @DisplayName("RED: createProduct - Nên lưu sản phẩm và trả về DTO")
    void createProduct_ShouldSaveAndReturnDTO() {
        // Arrange
        Product newProduct = createSampleProduct(null, createDTO.name(), createDTO.price(), createDTO.stock());
        Product savedProduct = createSampleProduct(2L, createDTO.name(), createDTO.price(), createDTO.stock());

        // Giả lập Repository: Khi gọi .save() với bất kỳ Product nào, trả về savedProduct
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductResponseDTO result = productService.createProduct(createDTO);

        // Assert (Kiểm tra xem phương thức được gọi và kết quả đúng chưa)
        verify(productRepository, times(1)).save(any(Product.class));
        assertNotNull(result.id());
        assertEquals("PC Gaming", result.name());
        assertEquals(2500.0, result.price());
    }

    @Test
    @DisplayName("RED: getProduct - Nên tìm thấy sản phẩm theo ID")
    void getProduct_ShouldReturnProduct_WhenIdExists() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // Act
        Optional<ProductResponseDTO> result = productService.getProduct(productId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().name());
    }

    // Test cho Pagination
    @Test
    @DisplayName("RED: getAllProducts - Nên trả về Pageable list of products")
    void getAllProducts_ShouldReturnPaginatedList() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        List<Product> productList = Arrays.asList(
            createSampleProduct(1L, "P1", 10.0, 1),
            createSampleProduct(2L, "P2", 20.0, 2)
        );
        Page<Product> productPage = new PageImpl<>(productList, pageable, 2);

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // Act
        Page<ProductResponseDTO> result = productService.getAllProducts(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals("P1", result.getContent().get(0).name());
        verify(productRepository, times(1)).findAll(pageable);
    }

    // Test các CRUD còn lại (Update, Delete) tương tự...

    // Yêu cầu: Tên sản phẩm không được null/empty, giá > 0, stock >= 0.

    // RED: Test createProduct thất bại khi validation đầu vào không hợp lệ
    @Test
    void testCreateProduct_ShouldThrowExceptionForInvalidData() {
        // 1. Tên null
        ProductCreateDTO invalidNameDto = new ProductCreateDTO(null, 10.0, 5);
        // RED: Giả định sẽ ném ra ngoại lệ khi validate (chưa có code)
        assertThrows(IllegalArgumentException.class,
            () -> productService.createProduct(invalidNameDto),
            "RED: Test should fail for null product name");

        // 2. Giá <= 0
        ProductCreateDTO invalidPriceDto = new ProductCreateDTO("Test Product", 0.0, 5);
        assertThrows(IllegalArgumentException.class,
            () -> productService.createProduct(invalidPriceDto),
            "RED: Test should fail for price <= 0");
    }

    // RED: Test updateProduct thất bại khi validation đầu vào không hợp lệ
    @Test
    void testUpdateProduct_ShouldThrowExceptionForInvalidData() {
        // 1. Stock < 0
        ProductUpdateDTO invalidStockDto = new ProductUpdateDTO("Updated Name", 10.0, -1);

        // Cần giả lập sản phẩm tồn tại để logic update được gọi
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(new Product()));

        // RED: Giả định sẽ ném ra ngoại lệ khi validate
        assertThrows(IllegalArgumentException.class,
            () -> productService.updateProduct(1L, invalidStockDto),
            "RED: Test should fail for stock < 0");
    }
}
