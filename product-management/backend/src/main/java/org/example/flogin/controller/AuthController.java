package org.example.flogin.controller;

import jakarta.validation.Valid;
import org.example.flogin.dto.JwtAuthResponseDTO;
import org.example.flogin.dto.LoginDTO;
import org.example.flogin.service.IAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {

        // Gọi placeholder validation
        if (!authService.validateUser(loginDTO.username(), loginDTO.password())) {
            // Trả về lỗi nếu validation thất bại
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Thực hiện Login và tạo token
        String token = authService.login(loginDTO);
        return ResponseEntity.ok(new JwtAuthResponseDTO(token));
    }
}