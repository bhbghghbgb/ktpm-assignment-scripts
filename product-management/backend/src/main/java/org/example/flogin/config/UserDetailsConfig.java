package org.example.flogin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserDetailsConfig {

    /**
     * 1. Cấu hình UserDetailsService (Giả định In-memory User)
     * Thiết lập tài khoản cứng theo yêu cầu: testuser/Test123
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
            .username("testuser")
            // Mật khẩu đã được mã hóa (BCrypt: Test123)
            .password(passwordEncoder.encode("Test123"))
            .roles("USER", "ADMIN") // Cho phép quyền cao nhất để test CRUD
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 4. Các Bean cơ bản khác
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}