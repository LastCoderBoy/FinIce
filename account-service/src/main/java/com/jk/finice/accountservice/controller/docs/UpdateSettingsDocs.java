package com.jk.finice.accountservice.controller.docs;

import com.jk.finice.accountservice.dto.response.AccountSettingsResponse;
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
        summary = "Update account settings",
        description = """
                Update account nickname and/or transaction limits.
                
                **Updatable Fields:**
                - **Nickname**: Custom name for the account (max 100 chars)
                - **Daily Withdrawal Limit**: Max cash withdrawal per day
                - **Daily Transfer Limit**: Max transfer amount per day
                
                **Validation Rules:**
                - At least one field must be provided
                - Limits cannot exceed account type maximum:
                  - Savings withdrawal: $500/day
                  - Savings transfer: $2,000/day
                  - Current withdrawal: $5,000/day
                  - Current transfer: $50,000/day
                - Users can decrease limits for security
                - Users cannot increase beyond type maximum
                
                **Security:**
                - Only account owner can update settings
                - Cannot update closed accounts
                
                **Authentication:**
                - Requires valid JWT token
                """
        ,
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Settings updated successfully",
                content = @Content(schema = @Schema(implementation = AccountSettingsResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid input - Limit exceeds maximum or no fields provided",
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
public @interface UpdateSettingsDocs {
}
