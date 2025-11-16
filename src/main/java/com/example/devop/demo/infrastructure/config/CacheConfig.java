package com.example.devop.demo.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.user-ttl:5}")
    private int userTtl;

    @Value("${cache.user-max-size:500}")
    private int userMaxSize;

    @Value("${cache.product-ttl:15}")
    private int productTtl;

    @Value("${cache.product-max-size:2000}")
    private int productMaxSize;

    @Value("${cache.default-ttl:10}")
    private int defaultTtl;

    @Value("${cache.default-max-size:1000}")
    private int defaultMaxSize;

//    @Bean
//    @Primary
//    public CacheManager cacheManager() {
//        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
//        cacheManager.setCaffeine(Caffeine.newBuilder()
//                .expireAfterWrite(10, TimeUnit.MINUTES)
//                .maximumSize(1000));
//        return cacheManager;
//    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                buildCache("userCache", userTtl, userMaxSize),
                buildCache("productCache", productTtl, productMaxSize),
                buildCache("defaultCache", defaultTtl, defaultMaxSize)
        ));
        return cacheManager;
    }

    private CaffeineCache buildCache(String name, int minutesToExpire, int maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(minutesToExpire, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .recordStats()
                .build());
    }
}
