package com.jk.finice.authservice.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Logout",
        description = """
                Invalidate current access token and refresh token.
                
                **Actions:**
                - Revoke Refresh token
                - Access token added to Redis Blacklist Cache (expires naturally)
                - Refresh token cookie cleared
                
                **Authentication:**
                - Requires valid JWT token
                
                **Note:**
                - Access Token remains blacklisted until its natural expiration
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Logout successful",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface LogoutDocs {
}
