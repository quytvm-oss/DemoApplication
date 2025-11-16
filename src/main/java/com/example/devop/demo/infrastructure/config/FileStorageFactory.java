package com.example.devop.demo.infrastructure.config;

import com.example.devop.demo.infrastructure.file.IFileStorage;
import com.example.devop.demo.infrastructure.file.LocalFileStorage;
import com.example.devop.demo.infrastructure.file.StorageProperties;
import org.springframework.stereotype.Component;

@Component
public class FileStorageFactory {

    private final StorageProperties properties;
    private final LocalFileStorage localFileStorage;
    // TODO: S3FileStorage s3FileStorage;

    public FileStorageFactory(StorageProperties properties, LocalFileStorage localFileStorage) {
        this.properties = properties;
        this.localFileStorage = localFileStorage;
    }

    public IFileStorage getStorage() {
        switch (properties.getType().toLowerCase()) {
            case "s3":
                // return s3FileStorage;
            default:
                return localFileStorage;
        }
    }
}
