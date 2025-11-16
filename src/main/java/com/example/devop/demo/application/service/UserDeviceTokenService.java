package com.example.devop.demo.application.service;

import com.example.devop.demo.application.dto.request.CreateUserDeviceTokenRequest;
import com.example.devop.demo.application.dto.response.CreateUserDeviceTokenDto;

public interface UserDeviceTokenService {
    CreateUserDeviceTokenDto createUserDeviceToken(CreateUserDeviceTokenRequest request);
}
