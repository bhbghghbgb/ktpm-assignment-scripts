package org.example.flogin.service;

import org.example.flogin.config.JwtTokenProvider;
import org.example.flogin.dto.LoginDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String login(LoginDTO loginDTO) {

        // 1. Xác thực bằng Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password())
        );

        // 2. Thiết lập Security Context và tạo JWT
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }

    // Placeholder validation method (cho yêu cầu bài tập)
    @Override
    public boolean validateUser(String username, String password) {
        // Phương thức này sẽ được TDD/Kiểm thử sau, hiện tại chỉ là placeholder.
        // Có thể dùng để kiểm tra các luật như: độ dài password, format username,...
//        if (username == null || username.length() < 5) return false;
//        if (password == null || password.length() < 8) return false;
        return true;
    }
}
