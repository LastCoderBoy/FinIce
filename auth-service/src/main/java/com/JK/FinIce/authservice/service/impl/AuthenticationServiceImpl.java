package com.JK.FinIce.authservice.service.impl;

import com.JK.FinIce.authservice.config.redis.RedisService;
import com.JK.FinIce.authservice.config.security.JwtProvider;
import com.JK.FinIce.authservice.dto.*;
import com.JK.FinIce.authservice.entity.RefreshToken;
import com.JK.FinIce.authservice.entity.Role;
import com.JK.FinIce.authservice.entity.User;
import com.JK.FinIce.authservice.entity.UserPrincipal;
import com.JK.FinIce.authservice.enums.AccountStatus;
import com.JK.FinIce.authservice.exception.DuplicateResourceFoundException;
import com.JK.FinIce.authservice.queryService.RoleQueryService;
import com.JK.FinIce.authservice.repository.UserRepository;
import com.JK.FinIce.authservice.service.AuthenticationService;
import com.JK.FinIce.authservice.service.RefreshTokenService;
import com.JK.FinIce.authservice.utils.HeaderExtractor;
import com.JK.FinIce.commonlibrary.exception.InternalServerException;
import com.JK.FinIce.commonlibrary.exception.InvalidTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.JK.FinIce.authservice.mapper.UserMapper.mapToAuthResponse;
import static com.JK.FinIce.commonlibrary.constants.AppConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final RoleQueryService roleQueryService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional(rollbackFor = Exception.class)  // Rollback on ANY exception
    @Override
    public AuthResponse register(RegisterRequest registerRequest, HttpServletResponse response, HttpServletRequest request) {
        try {
            log.info("[AUTH-SERVICE] Starting registration for user: {}", registerRequest.getUsername());

            validateUsernameAndEmail(registerRequest.getUsername(), registerRequest.getEmail());

            // Extract Headers
            String clientIP = HeaderExtractor.extractClientIp(request);
            String userAgent = HeaderExtractor.extractUserAgent(request);

            // Create and Save User
            User newUser = createUserEntity(registerRequest, clientIP);
            newUser = userRepository.save(newUser);
            log.info("[AUTH-SERVICE] User saved with ID: {}", newUser.getId());

            // Generate Access Token
            List<String> roleNames = newUser.getRoles().stream()
                    .map(role -> role.getName().name())
                    .toList();

            String accessToken = jwtProvider.generateAccessToken(
                    newUser.getUsername(),
                    newUser.getId(),
                    newUser.getEmail(),
                    roleNames
            );

            // Create and Save Refresh Token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    newUser,
                    clientIP,
                    userAgent
            );

            // Set the Refresh token cookie
            setRefreshTokenCookie(response, refreshToken.getToken());

            // TODO: Send verification email
            // emailService.sendVerificationEmail(newUser.getEmail(), verificationToken);
            log.info("[AUTH-SERVICE] User registered successfully: {} (ID: {})",
                    newUser.getUsername(), newUser.getId());

            return mapToAuthResponse(newUser, accessToken);
        } catch (DuplicateResourceFoundException de) {
            log.warn("[AUTH-SERVICE] Registration failed - duplicate resource: {}", de.getMessage());
            throw de;
        } catch (InvalidTokenException e) {
            log.error("[AUTH-SERVICE] Token generation failed: {}", e.getMessage(), e);
            throw new InternalServerException("Failed to generate authentication tokens");
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Unexpected error during registration: {}", e.getMessage(), e);
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    @Override
    public UserResponse getProfile(UserPrincipal principal) {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public void logout(UserPrincipal principal, HttpServletResponse response, HttpServletRequest request) {

    }

    @Override
    public UserResponse updateUserProfile(UpdateUserRequest updateUserRequest, UserPrincipal principal, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public void changePassword(ChangePasswordRequest passwordRequest, UserPrincipal principal) {

    }

    private User createUserEntity(RegisterRequest registerRequest, String clientIP) {
        Role defaultRole = roleQueryService.getOrCreateDefaultRole(); // ROLE_USER

        User newUser = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // Hash password
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .emailVerified(false)
                .phoneVerified(false)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .lastLoginAt(LocalDateTime.now())
                .lastLoginIp(clientIP)
                .build();

        newUser.addRole(defaultRole);

        log.info("[AUTH-SERVICE] User Object with username: {} is updated successfully", registerRequest.getUsername());
        return newUser;
    }

    private void validateUsernameAndEmail(String username, String email) {
        boolean isUserExist = userRepository.existsByUsername(username);
        if (isUserExist) {
            log.warn("[AUTH-SERVICE] User with username {} already exists", username);
            throw new DuplicateResourceFoundException("User with username " + username + " already exists");
        }
        boolean isEmailExist = userRepository.existsByEmail(email);
        if (isEmailExist) {
            log.warn("[AUTH-SERVICE] User with email {} already exists", email);
            throw new DuplicateResourceFoundException("User with email " + email + " already exists");
        }
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) (REFRESH_TOKEN_DURATION_MS / 1000)); // Convert to seconds
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);

        log.debug("[AUTH-SERVICE] Set refresh token cookie");
    }
}
