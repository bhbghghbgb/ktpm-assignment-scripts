package org.example.flogin.dto;

public record JwtAuthResponseDTO(String accessToken, String tokenType) {
    public JwtAuthResponseDTO(String accessToken) {
        this(accessToken, "Bearer");
    }
}
