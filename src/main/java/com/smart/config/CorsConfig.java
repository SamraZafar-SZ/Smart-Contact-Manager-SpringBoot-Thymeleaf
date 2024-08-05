package com.smart.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();


        config.setAllowedOrigins(List.of("http://localhost:5174"));

       
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE","PATCH"));
        config.setAllowedHeaders(List.of("*"));


        config.addAllowedOrigin("http://localhost:5173"); 
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");



        source.registerCorsConfiguration("/**", config);
        return new CorsFilter((CorsConfigurationSource) source);
    }
}
