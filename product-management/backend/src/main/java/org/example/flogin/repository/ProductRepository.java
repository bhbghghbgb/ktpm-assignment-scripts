package org.example.flogin.repository;

import org.example.flogin.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository cung cấp sẵn các phương thức CRUD và Pagination
}