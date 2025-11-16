package com.example.devop.demo.application.mapper;

import com.example.devop.demo.application.dto.response.RefreshTokenDto;
import com.example.devop.demo.domain.user.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    @Mapping(target = "isActive", expression = "java(refreshToken.isActive())")
    @Mapping(target = "isRevoked", expression = "java(refreshToken.isRevoked())")
    @Mapping(target = "isExpired", expression = "java(refreshToken.isExpired())")
    RefreshTokenDto toRefreshTokenDto(RefreshToken refreshToken);
}
