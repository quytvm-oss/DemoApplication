package com.example.devop.demo.infrastructure.file;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileStorage implements IFileStorage{

    private final Path rootPath;

    public LocalFileStorage(StorageProperties properties) throws IOException {
        this.rootPath = Paths.get(properties.getBasePath()).toAbsolutePath();
        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }
    }

    @Override
    public void save(String filename, InputStream content) throws IOException {
        Path filePath = rootPath.resolve(filename);
        // tạo các thư mục cha nếu chưa tồn tại
        if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }
        Files.copy(content, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream load(String filename) throws IOException {
        return Files.newInputStream(rootPath.resolve(filename));
    }

    @Override
    public boolean delete(String filename) throws IOException {
        return Files.deleteIfExists(rootPath.resolve(filename));
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return Files.exists(rootPath.resolve(filename));
    }

    public Path getRootPath() {
        return rootPath;
    }
}
