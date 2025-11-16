package com.example.devop.demo.application.cqrs.commands.file;

import com.example.devop.demo.application.dto.response.UploadFileResult;
import com.example.devop.demo.shared.mediator.ICommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileCommand implements ICommand<List<UploadFileResult>> {
    private List<MultipartFile> files;
}
