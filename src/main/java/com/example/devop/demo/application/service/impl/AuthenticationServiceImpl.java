package com.example.devop.demo.application.service.impl;

import com.example.devop.demo.application.dto.request.*;
import com.example.devop.demo.application.dto.response.*;
import com.example.devop.demo.application.mapper.*;
import com.example.devop.demo.application.service.*;
import com.example.devop.demo.domain.user.*;
import com.example.devop.demo.infrastructure.authorization.*;
import com.example.devop.demo.infrastructure.authorization.metadata.*;
import com.example.devop.demo.infrastructure.persistence.*;
import com.example.devop.demo.shared.enums.ErrorCode;
import com.example.devop.demo.shared.exception.AppException;
import com.example.devop.demo.shared.utils.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    TokenService tokenService;
    UserService userService;
    CurrentRequest currentRequest;
    RefreshTokenRepository refreshTokenRepository;
    UserDeviceTokenService userDeviceTokenService;
    LoginHistoryRepository loginHistoryRepository;
    RefreshTokenMapper refreshTokenMapper;
    PasswordEncoder passwordEncoder;
    UserDeviceTokenRepository userDeviceTokenRepository;
    CurrentUser currentUser;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;

        // spotless:off
        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }
        //spotless:on

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository
                .findByUserNameOrContactNumber(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean isPermission = userService.hasPermission(user.getId(),
                Permission.nameFor(ResourceName.InternalUsers.name(), ActionName.LoginWebAdmin.name()));
        if (!isPermission)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        if (user.getIsLocked())
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);

        if (!user.getIsActive())
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String token = generateAccessToken(user);
        RefreshTokenDto refreshToken = generateRefreshToken(user,null);

        tokenService.addToken(token, refreshToken.getToken(), VALID_DURATION, REFRESHABLE_DURATION);
        user.setLastSignInDate(LocalDateTime.now());
        userRepository.save(user);

        if(StringUtils.hasText(request.getDeviceToken())){
            var createUserDeviceTokenRequest = new CreateUserDeviceTokenRequest(user.getId(),
                    request.getDeviceToken(), request.getPlatform());
            userDeviceTokenService.createUserDeviceToken(createUserDeviceTokenRequest);
        }

        var history = new LoginHistory();
        try {
            history.setDeviceName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        history.setLoginTime(LocalDateTime.now());
        String environment = System.getProperty("os.name") + " "
                + System.getProperty("os.version") + " ("
                + System.getProperty("os.arch") + ")";
        history.setEnvironment(environment);
        history.setUserId(user.getId());
        history.setIpConnected(currentRequest.getClientIp());
        loginHistoryRepository.save(history);

        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken.getToken())
                .authenticated(true)
                .build();
    }


    public void logout(LogoutRequest request) {
        try {
            tokenService.removeCurrentToken();
            //log.info("Token {} logged out and blacklisted for {} seconds", jti, ttl);
            if (StringUtils.hasText(request.getDeviceToken())){
                userDeviceTokenRepository
                        .findByUserIdAndToken(currentUser.getUserId(),
                                request.getDeviceToken()).ifPresent(userDeviceTokenRepository::delete);
            }
        } catch (AppException  e) {
            log.info("Token already expired or invalid");
        }
    }

    @Override
    public AuthenticationResponse refreshing(RefreshRequest request) {
        try {
            SignedJWT signedJWT = verifyAccessToken(request.getAccessToken());
            String userIdStr = signedJWT.getJWTClaimsSet().getSubject();
            if (userIdStr == null) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            Long userId = Long.parseLong(userIdStr);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            var newAccessToken = generateAccessToken(user);
            var newRefreshToken = generateRefreshToken(user, request.getRefreshToken());

            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken.getToken())
                    .authenticated(true)
                    .build();

        } catch (ParseException e) {
            throw new AppException(ErrorCode.USERNAME_INVALID);
        }
    }

    @Override
    public void revoke(RevokeRefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(()-> new AppException(ErrorCode.UNAUTHENTICATED));
        if (!refreshToken.isRefreshTokenValid(REFRESHABLE_DURATION)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    private void verifyToken(String token) {
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

            SignedJWT signedJWT = SignedJWT.parse(token);

            var verified = signedJWT.verify(verifier);

            if (!verified) throw new AppException(ErrorCode.UNAUTHENTICATED);

            var isActive = tokenService.isActive(token);
            if (!isActive) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

        }  catch (ParseException | JOSEException | IllegalArgumentException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private SignedJWT verifyAccessToken(String accessToken) {
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(accessToken);

            if (!signedJWT.verify(verifier)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return signedJWT;
        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private RefreshTokenDto generateRefreshToken(User user, String RefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByUserIdAndToken(user.getId(), RefreshToken)
                .orElse(null);

        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setUserId(user.getId());
        }
        else {
            if (!refreshToken.isRefreshTokenValid(REFRESHABLE_DURATION)) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }
        }

        String newToken = RefreshTokenUtil.generateRefreshToken();
        refreshToken.setToken(newToken);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiredAt(LocalDateTime.now().plusDays(REFRESHABLE_DURATION));
        refreshToken.setCreatedByIp(RefreshTokenUtil.getServerIpAddress());
        refreshTokenRepository.save(refreshToken);

        //remove tokens
        List<RefreshToken> refreshtokens =  refreshTokenRepository.findAllByUserId(user.getId());
        if (refreshtokens != null && !refreshtokens.isEmpty()) {
            List<RefreshToken> expiredTokens = refreshtokens.stream()
                    .filter(rt -> !rt.isRefreshTokenValid(REFRESHABLE_DURATION))
                    .toList();
            if (!expiredTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(expiredTokens);
            }
        }

        return refreshTokenMapper.toRefreshTokenDto(refreshToken);
    }

    private String generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserName())
                .issuer("quytvm.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim(Claims.USER_ID, user.getId())
                .claim(Claims.USERNAME, user.getUserName())
                .claim(Claims.EMAIL, user.getEmail())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
//                if (!CollectionUtils.isEmpty(role.getClaims())) {
//                    role.getClaims().forEach(permission -> stringJoiner.add(permission.getClaimValue()));
//                }
            });
        }
        return stringJoiner.toString();
    }
}
