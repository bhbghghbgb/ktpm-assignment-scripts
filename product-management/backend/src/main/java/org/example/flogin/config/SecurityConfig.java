package org.example.flogin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
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
     * 2. Cấu hình HTTP Security (Phân quyền API)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF (Thường dùng cho Stateless API)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Áp dụng CORS
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập public cho Login và Swagger/OpenAPI
                .requestMatchers("/api/auth/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**").permitAll()
                // Tất cả các request khác phải được xác thực
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    /**
     * 3. Cấu hình CORS
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*"); // Cho phép mọi nguồn (Cần thay đổi trong môi trường Production)
        config.addAllowedHeader("*"); // Cho phép mọi header
        config.addAllowedMethod("*"); // Cho phép mọi phương thức (GET, POST, PUT, DELETE)
        source.registerCorsConfiguration("/**", config); // Áp dụng cho mọi đường dẫn
        return source;
    }

    /**
     * 4. Các Bean cơ bản khác
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}