package com.jk.finice.authservice.controller.docs;

import com.jk.finice.authservice.dto.UserResponse;
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
        summary = "Get user profile",
        description = """
                Retrieve authenticated user's profile information.
                
                **Authentication:**
                - Requires valid JWT token in Authorization header
                
                **Returns:**
                - User details (id, username, email, name, roles, etc.)
                - Account status and verification flags
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Profile retrieved successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface GetProfileDocs {
}
