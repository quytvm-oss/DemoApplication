package com.example.devop.demo.infrastructure.file;

import java.io.IOException;
import java.io.InputStream;

public interface IFileStorage {
    void save(String filename, InputStream content) throws IOException;
    InputStream load(String filename) throws IOException;
    boolean delete(String filename) throws IOException;
    boolean exists(String filename) throws IOException;
}
