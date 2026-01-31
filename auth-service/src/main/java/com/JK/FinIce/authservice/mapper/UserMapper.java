package com.JK.FinIce.authservice.mapper;

import com.JK.FinIce.authservice.dto.AuthResponse;
import com.JK.FinIce.authservice.entity.User;

import java.util.stream.Collectors;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.ACCESS_TOKEN_DURATION_MS;

public class UserMapper {

    public static AuthResponse mapToAuthResponse(User user, String accessToken){
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
                )
                .build();

        return AuthResponse.builder()
                .user(userInfo)
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(ACCESS_TOKEN_DURATION_MS / 1000)
                .build();
    }
}
