package com.jk.finice.authservice.controller.docs;

import com.jk.finice.authservice.dto.response.AuthResponse;
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
        summary = "User login",
        description = """
                Authenticate user with email and password.
                
                **Returns:**
                - JWT access token (in response body)
                - Refresh token (in HTTP-only cookie)
                
                **Authentication:**
                - No JWT required for this endpoint
                
                **Rate Limiting:**
                - Max 5 attempts per minute per IP
                """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Account not verified or locked",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "429",
                description = "Too many login attempts",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface LoginDocs {
}
