package org.example.flogin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Kiểm Thử Phần Mềm - Product Management API")
                .version("1.0")
                .description("API cho chức năng Đăng nhập và Quản lý Sản phẩm. Dùng để thực hiện Kiểm thử Back-end.")
            );
    }
}