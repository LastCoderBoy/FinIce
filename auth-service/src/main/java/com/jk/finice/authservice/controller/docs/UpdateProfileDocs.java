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
        summary = "Update user profile",
        description = """
                Update user's username, first name, last name, or phone number.
                
                **Authentication:**
                - Requires valid JWT token
                
                **Updatable Fields:**
                - Username
                - First name
                - Last name
                - Phone number
                
                **Note:**
                - At least one field must be provided
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Profile updated successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface UpdateProfileDocs {
}
