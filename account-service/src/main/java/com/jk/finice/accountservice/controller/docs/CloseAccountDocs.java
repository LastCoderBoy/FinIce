package com.jk.finice.accountservice.controller.docs;

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
        summary = "Close account",
        description = """
                Permanently close an account (soft delete).
                
                **Pre-Conditions:**
                - Account balance must be exactly $0.00
                - No pending transactions (hold amount = $0.00)
                - Account must be ACTIVE (not already closed)
                
                **What Happens:**
                - Account status changed to CLOSED
                - Closure timestamp recorded
                - Closure reason stored
                - Account remains in database (audit trail)
                - Cannot be reopened
                
                **Recommended Steps Before Closing:**
                1. Transfer all funds to another account
                2. Cancel all scheduled payments
                3. Wait for all pending transactions to complete
                4. Download account statements for records
                
                **Security:**
                - Only account owner can close account
                - Requires closure reason
                
                **Authentication:**
                - Requires valid JWT token
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Account closed successfully",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Cannot close account - Non-zero balance or pending transactions",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not own this account",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface CloseAccountDocs {
}
