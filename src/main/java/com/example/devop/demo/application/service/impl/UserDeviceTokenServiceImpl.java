package com.example.devop.demo.application.service.impl;

import com.example.devop.demo.application.dto.request.CreateUserDeviceTokenRequest;
import com.example.devop.demo.application.dto.response.CreateUserDeviceTokenDto;
import com.example.devop.demo.application.service.UserDeviceTokenService;
import com.example.devop.demo.domain.user.UserDeviceToken;
import com.example.devop.demo.infrastructure.idGen.SnowflakeIdCustomGenerator;
import com.example.devop.demo.infrastructure.persistence.UserDeviceTokenRepository;
import com.example.devop.demo.infrastructure.persistence.UserRepository;
import com.example.devop.demo.shared.enums.ErrorCode;
import com.example.devop.demo.shared.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDeviceTokenServiceImpl implements UserDeviceTokenService {

    UserDeviceTokenRepository userDeviceTokenRepository;
    UserRepository userRepository;

    @Override
    public CreateUserDeviceTokenDto createUserDeviceToken(CreateUserDeviceTokenRequest request) {
        var user = userRepository.findById(request.getUserId());
        if (user.isEmpty())
            throw new AppException(ErrorCode.USER_NOT_EXISTED);

        var existedToken = userDeviceTokenRepository.findByUserIdAndToken(request.getUserId(), request.getToken())
                .orElse(null);
        if (existedToken != null) {
            existedToken.setUserId(request.getUserId());
            userDeviceTokenRepository.save(existedToken);
            return new CreateUserDeviceTokenDto(existedToken.getId(), existedToken.getUserId(),
                    existedToken.getToken(), existedToken.getPlatform());
        }

        UserDeviceToken deviceToken = new UserDeviceToken();
        deviceToken.setId(SnowflakeIdCustomGenerator.nextId());
        deviceToken.setUserId(request.getUserId());
        deviceToken.setToken(request.getToken());
        deviceToken.setPlatform(request.getPlatform());
        userDeviceTokenRepository.save(deviceToken);

        return new CreateUserDeviceTokenDto(deviceToken.getId(), deviceToken.getUserId(),
                deviceToken.getToken(), deviceToken.getPlatform());
    }
}
