package org.example.flogin.dto;

// Dùng cho request POST /api/products
public record ProductCreateDTO(String name, Double price, Integer stock) {}
