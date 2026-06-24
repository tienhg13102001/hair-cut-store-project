package com.haircut.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS = Cross-Origin Resource Sharing. Trình duyệt CHẶN request từ origin khác
// (FE http://localhost:3000) tới backend (http://localhost:8080) trừ khi server
// cho phép rõ ràng. Config này khai báo các origin/method được phép.
// LƯU Ý: chỉ mở cho dev. Production phải đổi sang domain thật, KHÔNG để "*".
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // áp dụng mọi endpoint
        .allowedOrigins("http://localhost:3000") // FE Next.js dev
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }
}
