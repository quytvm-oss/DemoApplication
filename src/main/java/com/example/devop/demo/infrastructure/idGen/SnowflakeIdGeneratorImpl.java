package com.example.devop.demo.infrastructure.idGen;

import jakarta.persistence.Id;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
public class SnowflakeIdGeneratorImpl  implements IdentifierGenerator {
//    @Override
//    public Object generate(SharedSessionContractImplementor session, Object object) {
//        long id = SnowflakeIdCustomGenerator.nextId();
//        log.debug("Generated Snowflake ID: {}", id);
//        return id;
//    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object entity) {
        // ✅ KIỂM TRA: Nếu ID đã được set, GIỮ NGUYÊN
        try {
            Field idField = findIdField(entity.getClass());
            if (idField != null) {
                idField.setAccessible(true);
                Object existingId = idField.get(entity);

                if (existingId instanceof Long && ((Long) existingId) > 0) {
                    log.debug("✅ Using manually set ID: {} for {}",
                            existingId, entity.getClass().getSimpleName());
                    return existingId; // GIỮ NGUYÊN ID
                }
            }
        } catch (Exception e) {
            log.warn("Error checking existing ID", e);
        }

        // ❌ Nếu chưa có ID, TỰ ĐỘNG SINH
        long id = SnowflakeIdCustomGenerator.nextId();
        log.debug("🆕 Generated new ID: {} for {}",
                id, entity.getClass().getSimpleName());
        return id;
    }

    private Field findIdField(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
