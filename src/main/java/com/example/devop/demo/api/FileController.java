package com.example.devop.demo.api;

import com.example.devop.demo.application.cqrs.commands.file.UploadFileCommand;
import com.example.devop.demo.application.dto.response.UploadFileResult;
import com.example.devop.demo.shared.mediator.IMediator;
import com.example.devop.demo.shared.resposne.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    private final IMediator mediator;

    @PostMapping("/upload")
    ApiResponse<List<UploadFileResult>> upload(@RequestParam("files") List<MultipartFile> files)  {
        var request = new UploadFileCommand(files);
        var result = mediator.send(request);
        return ApiResponse.<List<UploadFileResult>>builder()
                .result(result)
                .build();
    }

}
