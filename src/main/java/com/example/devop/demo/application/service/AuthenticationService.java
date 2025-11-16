package com.example.devop.demo.application.service;

import com.example.devop.demo.application.dto.request.*;
import com.example.devop.demo.application.dto.response.AuthenticationResponse;
import com.example.devop.demo.application.dto.response.IntrospectResponse;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void logout(LogoutRequest request);
    AuthenticationResponse refreshing(RefreshRequest request);
    void revoke(RevokeRefreshTokenRequest request);
}
