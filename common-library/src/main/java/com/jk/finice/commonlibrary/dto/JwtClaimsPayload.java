package com.jk.finice.commonlibrary.dto;

import java.util.List;

public record JwtClaimsPayload(
        Long userId,
        String email,
        List<String> roles
) {}
