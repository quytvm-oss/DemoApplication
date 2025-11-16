package com.example.devop.demo.infrastructure.config;

import com.example.devop.demo.infrastructure.file.StorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {
    private final StorageProperties properties;

    public FileConfig(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(properties.getUrlBase() + "/**")
                .addResourceLocations("file:" + properties.getBasePath() + "/")
                .setCachePeriod(3600) // optional caching
                .resourceChain(true);
    }
}
