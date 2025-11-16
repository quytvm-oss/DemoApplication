package com.example.devop.demo.api;

import com.example.devop.demo.application.cqrs.commands.customer.CreateCustomerCommand;
import com.example.devop.demo.shared.mediator.IMediator;
import com.example.devop.demo.shared.resposne.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {
    private final IMediator mediator;


    @PostMapping
    ApiResponse<Long> create(@RequestBody CreateCustomerCommand req) {
        var result = mediator.send(req);
        return ApiResponse.<Long>builder()
                .result(result)
                .build();
    }
}
