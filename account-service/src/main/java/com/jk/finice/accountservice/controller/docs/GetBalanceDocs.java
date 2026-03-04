package com.jk.finice.accountservice.controller.docs;

import com.jk.finice.accountservice.dto.response.BalanceResponse;
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
        summary = "Get account balance",
        description = """
                Get current balance information for an account.
                
                **Balance Types:**
                - **Total Balance**: Actual money in the account
                - **Available Balance**: Money you can use right now (balance - holds)
                - **Hold Amount**: Money reserved for pending transactions
                
                **Formula:**
                - Available Balance = Total Balance - Hold Amount
                
                **Use Cases:**
                - Check available funds before making a transaction
                - View pending holds
                - Monitor account balance
                
                **Security:**
                - Only account owner can view balance
                - Cannot view balance of closed accounts
                
                **Authentication:**
                - Requires valid JWT token
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Balance retrieved successfully",
                content = @Content(schema = @Schema(implementation = BalanceResponse.class))
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
public @interface GetBalanceDocs {
}
