package com.JK.FinIce.authservice.controller;


import com.JK.FinIce.authservice.dto.*;
import com.JK.FinIce.authservice.entity.UserPrincipal;
import com.JK.FinIce.commonlibrary.dto.ApiResponse;
import com.JK.FinIce.commonlibrary.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.AUTHORIZATION_HEADER;
import static com.JK.FinIce.commonlibrary.constants.AppConstants.AUTH_PATH;

/**
 * Authentication Controller
 * Handles register, login, logout, and user updates
 *
 * @author LastCoderBoy
 * @since 2026-01-26
 */
@RestController
@RequestMapping(AUTH_PATH)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                              HttpServletResponse response,
                                                              HttpServletRequest request) {
        // Base validation is done via @Valid annotation
        // processing the request to the service layer
        log.info("[AUTH-CONTROLLER] Registering user: {}", registerRequest.getUsername());

        UserResponse userResponse = authService.register(registerRequest, response, request);

        return ResponseEntity.ok(
                ApiResponse.success("User registered successfully", userResponse)
        );
    }

    // Better to use the AuthenticationPrincipal annotation rather than
    // validating the Access Token in the authorization header
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal){
        log.info("[AUTH-CONTROLLER] Getting user details...");

        UserResponse userResponse = authService.getProfile(principal);
        return ResponseEntity.ok(
                ApiResponse.success("User details retrieved successfully", userResponse)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {
        log.info("[AUTH-CONTROLLER] Logging in user: {}", loginRequest.getUsernameOrEmail());

        AuthResponse authResponse = authService.login(loginRequest, request, response);
        return ResponseEntity.ok(ApiResponse.success("User logged in successfully", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletResponse response, // used for clearing the cookies
                                                    HttpServletRequest request) {
        log.info("[AUTH-CONTROLLER] Logging out user...");

        authService.logout(principal, response, request);
        return ResponseEntity.ok(ApiResponse.success("User logged out successfully"));
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<UserResponse>> update(@RequestBody UpdateUserRequest updateUserRequest,
                                                            @AuthenticationPrincipal UserPrincipal principal,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response){
        log.info("[AUTH-CONTROLLER] Updating user details...");

        UserResponse userResponse = authService.update(updateUserRequest, principal, request, response);

        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userResponse));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest passwordRequest,
                                                            @AuthenticationPrincipal UserPrincipal principal){
        log.info("[AUTH-CONTROLLER] Changing password for user: {}", principal.getUsername());

        authService.changePassword(passwordRequest, principal);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
