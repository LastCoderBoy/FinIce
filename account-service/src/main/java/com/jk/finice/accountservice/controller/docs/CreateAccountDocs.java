package com.jk.finice.accountservice.controller.docs;

import com.jk.finice.accountservice.dto.response.AccountResponse;
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
        summary = "Create new account",
        description = """
                Create a new savings or current account for authenticated user.
                
                **Account Type Limits:**
                - Maximum 1 CURRENT account per user
                - Maximum 5 SAVINGS accounts per user
                
                **Initial Deposit:**
                - Minimum: $10.00
                - Maximum: $1,000,000.00
                
                **Account Features:**
                - Unique IBAN generated automatically
                - Default transaction limits based on account type
                - Interest rate applied for savings accounts
                
                **Authentication:**
                - Requires valid JWT token in Authorization header
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Account created successfully",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid input or account limit reached",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface CreateAccountDocs {
}