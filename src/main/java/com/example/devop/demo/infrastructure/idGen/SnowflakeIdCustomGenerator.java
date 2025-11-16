package com.example.devop.demo.infrastructure.idGen;

import de.mkammerer.snowflakeid.SnowflakeIdGenerator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SnowflakeIdCustomGenerator {

    @Value("${snowflake.generator-id:1}")
    private int generatorId;

    private static SnowflakeIdGenerator staticGenerator;

    @PostConstruct
    public void init() {
        staticGenerator = SnowflakeIdGenerator.createDefault(generatorId);
        log.info("✅ Snowflake ID Generator initialized with generatorId: {}", generatorId);
    }

    public static long nextId() {
        if (staticGenerator == null) {
            throw new IllegalStateException("Generator not initialized. Make sure Spring context is loaded.");
        }
        return staticGenerator.next();
    }
}