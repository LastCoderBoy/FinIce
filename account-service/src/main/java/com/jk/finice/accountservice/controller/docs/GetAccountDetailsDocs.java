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
        summary = "Get account details",
        description = """
                Retrieve detailed information for a specific account.
                
                **Returns:**
                - Full account details including:
                  - IBAN
                  - Account type and nickname
                  - Balance information
                  - Transaction limits
                  - Interest rate (for savings)
                  - Account status
                  - Creation and last update timestamps
                
                **Security:**
                - Only account owner can view details
                - Closed accounts can still be viewed
                
                **Authentication:**
                - Requires valid JWT token
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Account details retrieved successfully",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))
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
public @interface GetAccountDetailsDocs {
}
