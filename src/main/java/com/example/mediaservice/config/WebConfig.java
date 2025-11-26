package com.example.mediaservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Cấu hình CORS cho toàn bộ API
        registry.addMapping("/**") // Cấu hình cho tất cả các endpoint
                .allowedOrigins("http://localhost:3000","http://localhost:5100") // Các domain được phép truy cập
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Các phương thức được phép
                .allowedHeaders("Content-Type", "Authorization", "Accept", "X-Requested-With", "Origin") // Các header được phép
                .exposedHeaders("Authorization", "Content-Type") // Các header mà client có thể truy cập
                .allowCredentials(true) // Cho phép gửi thông tin xác thực (cookies)
                .maxAge(3600); // Cấu hình thời gian cache pre-flight request
    }
}