package com.jk.finice.accountservice.controller.docs;

import com.jk.finice.accountservice.dto.response.AccountSummaryResponse;
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
        summary = "Get account summary",
        description = """
                Get aggregated summary of all user's accounts.
                
                **Summary Includes:**
                - Total number of accounts
                - Number of savings accounts
                - Number of current accounts
                - Total balance across all accounts
                - Total available balance
                - Total amount on hold
                - List of all accounts with details
                
                **Use Cases:**
                - Dashboard overview
                - Net worth calculation
                - Portfolio summary
                
                **Performance:**
                - Optimized single-query calculation
                - Calculated in-memory (max 6 accounts)
                
                **Authentication:**
                - Requires valid JWT token
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Summary retrieved successfully",
                content = @Content(schema = @Schema(implementation = AccountSummaryResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = com.jk.finice.commonlibrary.dto.ApiResponse.class))
        )
})
public @interface GetSummaryDocs {
}
