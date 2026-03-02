package com.jk.finice.authservice.controller.docs;

import com.jk.finice.authservice.dto.AuthResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Register new user",
        description = "Create a new user account with email, username, and password. " +
                "Returns JWT access token and refresh token cookie."
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "User registered successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid input or duplicate username/email",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface RegisterDocs {
}
