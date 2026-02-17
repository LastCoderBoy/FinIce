package com.jk.finice.authservice.service.impl;

import com.jk.finice.authservice.config.AuthCookiesManager;
import com.jk.finice.authservice.config.redis.RedisService;
import com.jk.finice.authservice.config.security.JwtProvider;
import com.jk.finice.authservice.dto.*;
import com.jk.finice.authservice.service.email.EmailService;
import com.jk.finice.authservice.entity.*;
import com.jk.finice.authservice.enums.AccountStatus;
import com.jk.finice.authservice.enums.TokenType;
import com.jk.finice.authservice.exception.AccountLockedException;
import com.jk.finice.authservice.exception.AccountNotVerifiedException;
import com.jk.finice.authservice.exception.DuplicateResourceFoundException;
import com.jk.finice.authservice.queryService.RoleQueryService;
import com.jk.finice.authservice.repository.UserRepository;
import com.jk.finice.authservice.service.AuthenticationService;
import com.jk.finice.authservice.service.EmailTokenService;
import com.jk.finice.authservice.service.RefreshTokenService;
import com.jk.finice.authservice.utils.HeaderExtractor;
import com.jk.finice.commonlibrary.exception.InternalServerException;
import com.jk.finice.commonlibrary.exception.InvalidTokenException;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import com.jk.finice.commonlibrary.exception.ValidationException;
import com.jk.finice.commonlibrary.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.jk.finice.authservice.mapper.UserMapper.mapToAuthResponse;
import static com.jk.finice.authservice.mapper.UserMapper.mapToUserResponse;
import static com.jk.finice.commonlibrary.constants.AppConstants.AUTHORIZATION_HEADER;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final EmailTokenService emailTokenService;
    private final EmailService emailService;
    private final AuthCookiesManager cookiesManager;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final RoleQueryService roleQueryService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    // =========== REPOSITORIES ===========
    private final UserRepository userRepository;

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
            cookiesManager.setRefreshTokenCookie(response, refreshToken.getToken());

            // Send verification email
            EmailToken verificationToken = emailTokenService.createEmailToken(newUser, TokenType.EMAIL_VERIFICATION);
            try {
                emailService.sendVerificationEmail(
                        newUser,
                        verificationToken
                );
            } catch (Exception e) {
                log.error("[AUTH-SERVICE] Failed to send verification email: {}", e.getMessage());
                // No need to break...
            }

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
    @Transactional(readOnly = true)
    public UserResponse getProfile(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> {
                    log.error("[AUTH-SERVICE] User not found with ID: {}", principal.getId());
                    return new ResourceNotFoundException("User not found with ID: " + principal.getId());
        });

        return mapToUserResponse(user);
    }

    // No need Transactional
    @Override
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            log.info("[AUTH-SERVICE] Authentication successful for user: {} (ID: {})",
                    principal.getUsername(), principal.getId());

            // Fetch full user entity
            User user = findUserById(principal.getId());

            String clientIp = HeaderExtractor.extractClientIp(request);
            String userAgent = HeaderExtractor.extractUserAgent(request);

            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(clientIp);
            user.resetFailedLoginAttempts();
            userRepository.save(user);

            // Generate Access Token
            String accessToken = jwtProvider.generateAccessToken(
                    principal.getUsername(),
                    principal.getId(),
                    principal.getEmail(),
                    principal.getListOfRoles()
            );

            // Generate Refresh Token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user, clientIp, userAgent
            );

            cookiesManager.setRefreshTokenCookie(response, refreshToken.getToken());

            return mapToAuthResponse(user, accessToken);

        } catch (DisabledException e) {
            log.warn("[AUTH-SERVICE] Account is not verified: {}", loginRequest.getUsernameOrEmail());

            throw new AccountNotVerifiedException("Account is still Pending. Please verify your email first.");
        } catch (AuthenticationException e) {
            log.warn("[AUTH-SERVICE] Login failed: {}", e.getMessage());

            updateFailedLoginAttempts(loginRequest.getUsernameOrEmail());
            throw new BadCredentialsException("Invalid credentials");
        } catch (AccountLockedException e) {
            log.warn("[AUTH-SERVICE] Account is locked: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Unexpected error during login: {}", e.getMessage(), e);
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    private void updateFailedLoginAttempts(String usernameOrEmail) {
        try {
            Optional<User> optionalUser = userRepository.findByUsernameOrEmail(usernameOrEmail);
            if(optionalUser.isPresent()){
                User user = optionalUser.get();
                user.incrementFailedLoginAttempts();
                userRepository.save(user);

                if(user.getFailedLoginAttempts() == 4){
                    throw new BadCredentialsException("Account will be locked after 1 more failed attempt");
                }

                if (user.getAccountLocked()) {
                    log.warn("[AUTH-SERVICE] Account locked due to failed attempts: {} (ID: {})",
                            user.getUsername(), user.getId());
                    throw new AccountLockedException("Account is locked for 30 minutes due to failed attempts");
                }
            }
        } catch (BadCredentialsException | AccountLockedException e) {
            // Re-throw
            throw e;
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Failed to update login attempts: {}", e.getMessage());
            // Don't fail login process if this fails
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(UserPrincipal principal, HttpServletResponse response, HttpServletRequest request) {
        try {
            // Revoke Refresh Token
            Optional<String> refreshToken = cookiesManager.extractRefreshTokenFromCookie(request);
            refreshToken.ifPresent(refreshTokenService::revokeRefreshToken);

            // Blacklist the Access Token in Redis
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            blacklistAccessToken(authHeader);

            cookiesManager.clearRefreshTokenCookie(response);
            log.info("[AUTH-SERVICE] Logout successful for user: {}", principal.getUsername());
        } catch (ResourceNotFoundException e) {
            log.warn("[AUTH-SERVICE] Logout failed: {}", e.getMessage());

            cookiesManager.clearRefreshTokenCookie(response);
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Unexpected error during logout: {}", e.getMessage());

            cookiesManager.clearRefreshTokenCookie(response);
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    private void blacklistAccessToken(String authHeader){
        if(authHeader != null){
            String accessToken = TokenUtils.validateAndExtractToken(authHeader);
            Date tokenExpiration = jwtProvider.getExpirationDateFromToken(accessToken);
            long remainingTtl = tokenExpiration.getTime() - System.currentTimeMillis();

            if (remainingTtl > 0) {
                redisService.blackListToken(accessToken, remainingTtl);
                log.debug("[AUTH-SERVICE] Access token blacklisted for {}ms", remainingTtl);
            } else {
                log.debug("[AUTH-SERVICE] Access token already expired, skipping blacklist");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateUserProfile(UpdateUserRequest updateUserRequest,
                                          UserPrincipal principal,
                                          HttpServletRequest request, HttpServletResponse response) {
        try{
            if(!updateUserRequest.isAtLeastOneFieldProvided()){
                throw new ValidationException("At least one field must be provided for update");
            }
            User userEntity = findUserById(principal.getId());

            populateEntityWithLatestData(userEntity, updateUserRequest);

            User updatedUser = userRepository.save(userEntity);
            log.info("[AUTH-SERVICE] User profile updated successfully for user: {}", principal.getUsername());

            return mapToUserResponse(updatedUser);

        } catch (DuplicateResourceFoundException | ValidationException de){
            throw de;
        } catch (Exception e){
            log.error("[AUTH-SERVICE] Unexpected error occurred while updating user profile: {}", e.getMessage());
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    private void populateEntityWithLatestData(User userEntity, UpdateUserRequest updateUserRequest) {

        if(updateUserRequest.getUsername() != null &&
                !updateUserRequest.getUsername().equals(userEntity.getUsername())){

            boolean alreadyPresent = userRepository.existsByUsername(updateUserRequest.getUsername());
            if(alreadyPresent){
                log.warn("[AUTH-SERVICE] Username {} already exists", updateUserRequest.getUsername());
                throw new DuplicateResourceFoundException("Username already exists");
            }
            userEntity.setUsername(updateUserRequest.getUsername());
        }

        if(updateUserRequest.getFirstName() != null){
            userEntity.setFirstName(updateUserRequest.getFirstName());
        }
        if(updateUserRequest.getLastName() != null){
            userEntity.setLastName(updateUserRequest.getLastName());
        }
        if(updateUserRequest.getPhoneNumber() != null){
            userEntity.setPhoneNumber(updateUserRequest.getPhoneNumber());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest passwordRequest, UserPrincipal principal, HttpServletResponse response) {
        try{
            User userEntity = findUserById(principal.getId());
            String savedPassword = userEntity.getPassword();
            boolean isCurrentPasswordValid = passwordEncoder.matches(passwordRequest.getCurrentPassword(), savedPassword);
            if(!isCurrentPasswordValid){
                throw new BadCredentialsException("Current password is incorrect");
            }

            // checks the match of the new password and confirm password
            if(!passwordRequest.isPasswordsMatch()){
                throw new ValidationException("Confirm password does not match with the new password");
            }

            if (passwordEncoder.matches(passwordRequest.getNewPassword(), savedPassword)) {
                throw new ValidationException("New password must be different from current password");
            }

            userEntity.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
            userEntity.setPasswordChangedAt(LocalDateTime.now());
            userRepository.save(userEntity);

            cookiesManager.clearRefreshTokenCookie(response);
            log.debug("[AUTH-SERVICE] Refresh token cookie cleared");

            refreshTokenService.revokeAllRefreshTokensAsync(principal.getId());

            log.info("[AUTH-SERVICE] Password changed successfully for user: {} (ID: {})",
                    principal.getUsername(), principal.getId());
        } catch (BadCredentialsException | ValidationException e) {
            log.warn("[AUTH-SERVICE] Password change failed for user {}: {}",
                    principal.getUsername(), e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            log.error("[AUTH-SERVICE] User not found during password change: {}", e.getMessage());
            throw e;
        } catch (Exception e){
            log.error("[AUTH-SERVICE] Unexpected error occurred while changing password: {}", e.getMessage());
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    /**
     * Logout from all devices asynchronously
     * Runs AFTER response is sent to client
     *
     * @param userId User ID
     * @param authHeader Authorization header (contains current access token)
     */
    @Transactional
    public void logoutAll(Long userId, String authHeader) {
        try{
            // Revoke Refresh Token
            refreshTokenService.revokeAllRefreshTokensAsync(userId);

            // Blacklist the Access Token in Redis
            if (authHeader != null) {
                blacklistAccessToken(authHeader);
            } else {
                log.warn("[AUTH-SERVICE] No authorization header found for blacklisting");
            }

            log.info("[AUTH-SERVICE] Logout all successful for user ID: {}", userId);

        } catch (Exception e){
            // Don't throw - this is async, exceptions are logged by AsyncUncaughtExceptionHandler
            log.error("[AUTH-SERVICE] Error during async logout for user {}: {}",
                    userId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshJwtTokens(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> optionalRefreshToken = cookiesManager.extractRefreshTokenFromCookie(request);
        if(optionalRefreshToken.isEmpty()){
            throw new ResourceNotFoundException("Request does not contain refresh token cookie");
        }
        // Extract Headers
        String clientIp = HeaderExtractor.extractClientIp(request);
        String userAgent = HeaderExtractor.extractUserAgent(request);

        // verify the old refresh token and create a new one
        String token = optionalRefreshToken.get();
        RefreshToken oldRefreshToken = refreshTokenService.verifyRefreshToken(token); // method will resolve the Lazy Exception

        User user = oldRefreshToken.getUser();
        validateUserAccountForTokenRefresh(user);

        // Generate new Refresh Token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                oldRefreshToken,
                clientIp,
                userAgent
        );

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userRepository.save(user);

        // Generate new Access Token
        String username = user.getUsername();
        Long userId = user.getId();
        String email = user.getEmail();
        List<String> userRoles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        String accessToken = jwtProvider.generateAccessToken(username, userId, email, userRoles);

        // Set the Refresh token cookie
        cookiesManager.setRefreshTokenCookie(response, newRefreshToken.getToken());

        return mapToAuthResponse(user, accessToken);
    }

    private void validateUserAccountForTokenRefresh(User user) {
        if(user.getAccountStatus() == AccountStatus.CLOSED ||
                user.getAccountStatus() == AccountStatus.SUSPENDED){
            throw new AccountLockedException("User account is " + user.getAccountStatus().name() + ". Cannot refresh token");
        }
        if(user.getAccountLocked()){
            if (user.getAccountLockedUntil() != null &&
                    LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
                // Lock expired, unlock
                user.resetFailedLoginAttempts();
            } else {
                throw new AccountLockedException("Account is locked");
            }
        }
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

        log.info("[AUTH-SERVICE] User Object with username: {} is populated successfully", registerRequest.getUsername());
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

    private User findUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
