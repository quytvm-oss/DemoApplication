package com.example.devop.demo.application.cqrs.handlers.file;

import com.example.devop.demo.application.cqrs.commands.file.UploadFileCommand;
import com.example.devop.demo.application.dto.response.UploadFileResult;
import com.example.devop.demo.infrastructure.file.IFileStorage;
import com.example.devop.demo.shared.exception.FileStorageException;
import com.example.devop.demo.shared.mediator.ICommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Service
public class UploadFileCommandHandler implements ICommandHandler<UploadFileCommand, List<UploadFileResult>> {

    private final IFileStorage fileStorage;

    public UploadFileCommandHandler(IFileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public List<UploadFileResult> handle(UploadFileCommand request)  {
        List<UploadFileResult> results = new ArrayList<>();
        for (MultipartFile file : request.getFiles()) {
            results.add(uploadSingleFile(file));
        }
        return results;
    }

    private UploadFileResult uploadSingleFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFileName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        String extension = getExtension(originalFileName);
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("Invalid file");
        }

        String uploadFolder = getUploadFolder(extension);
        String randomFolder = UUID.randomUUID().toString();
        String validFileName = getValidFileName(originalFileName);

        String path = Paths.get(uploadFolder, randomFolder, validFileName).toString();

        try {
            fileStorage.save(path, file.getInputStream());
        } catch (IOException e) {
            throw new FileStorageException("Failed to save file: " + path, e);
        }

        path =  path.replace("\\", "/");

        return new UploadFileResult(originalFileName, path);
    }

    private String getUploadFolder(String extension) {
        List<String> imageExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
        List<String> audioExtensions = Arrays.asList(".mp3", ".m4u");

        extension = extension.toLowerCase();
        if (imageExtensions.contains(extension)) return "images";
        if (audioExtensions.contains(extension)) return "audios";
        return "files";
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > 0 ? filename.substring(idx) : "";
    }

    private String getValidFileName(String filename) {
        return filename.trim().replaceAll("[^A-Za-z0-9_.]+", "");
    }
}
