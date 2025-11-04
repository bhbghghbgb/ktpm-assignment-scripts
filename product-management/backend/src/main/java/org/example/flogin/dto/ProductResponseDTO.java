package org.example.flogin.dto;

import java.time.LocalDateTime;

// Dùng cho response GET/POST/PUT
public record ProductResponseDTO(Long id, String name, Double price, Integer stock, LocalDateTime createdAt) {}