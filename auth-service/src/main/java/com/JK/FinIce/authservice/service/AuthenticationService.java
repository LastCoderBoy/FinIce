package com.JK.FinIce.authservice.service;

import com.JK.FinIce.authservice.dto.*;
import com.JK.FinIce.authservice.entity.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    AuthResponse register(RegisterRequest registerRequest,
                          HttpServletResponse response,
                          HttpServletRequest request);

    UserResponse getProfile(UserPrincipal principal);

    AuthResponse login(LoginRequest loginRequest,
                              HttpServletRequest request,
                              HttpServletResponse response);

    void logout(UserPrincipal principal,
                       HttpServletResponse response,
                       HttpServletRequest request);

    UserResponse updateUserProfile (UpdateUserRequest updateUserRequest, UserPrincipal principal,
                                   HttpServletRequest request, HttpServletResponse response);

    void changePassword(ChangePasswordRequest passwordRequest, UserPrincipal principal,
                        HttpServletRequest request, HttpServletResponse response);

    AuthResponse refreshJwtTokens(HttpServletRequest request, HttpServletResponse response);
}
