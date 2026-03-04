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
        summary = "Get all accounts",
        description = """
                Retrieve all active accounts for authenticated user.
                
                **Returns:**
                - List of all ACTIVE accounts (excludes CLOSED accounts)
                - Accounts ordered by creation date (newest first)
                
                **Account Information Includes:**
                - IBAN (masked for security)
                - Account type (SAVINGS/CURRENT)
                - Current balance
                - Available balance
                - Currency
                - Account status
                
                **Authentication:**
                - Requires valid JWT token
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Accounts retrieved successfully",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface GetAllAccountsDocs {
}
