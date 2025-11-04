package org.example.flogin.controller;

import org.example.flogin.dto.ProductCreateDTO;
import org.example.flogin.dto.ProductResponseDTO;
import org.example.flogin.dto.ProductUpdateDTO;
import org.example.flogin.service.IProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    // [CRUD: CREATE]
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO createProduct(@RequestBody ProductCreateDTO createDTO) {
        return productService.createProduct(createDTO);
    }

    // [CRUD: READ ALL] - Với Pagination
    @GetMapping
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        // Ví dụ: GET /api/products?page=0&size=10&sort=name,asc
        return productService.getAllProducts(pageable);
    }

    // [CRUD: READ ONE]
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        return productService.getProduct(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // [CRUD: UPDATE]
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
                                                            @RequestBody ProductUpdateDTO updateDTO) {
        try {
            ProductResponseDTO updatedProduct = productService.updateProduct(id, updateDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            // Thay thế bằng exception handler phù hợp sau này
            return ResponseEntity.notFound().build();
        }
    }

    // [CRUD: DELETE]
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}