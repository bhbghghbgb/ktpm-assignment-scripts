package org.example.flogin.dto;

// Dùng cho request PUT /api/products/{id}
public record ProductUpdateDTO(String name, Double price, Integer stock) {}
