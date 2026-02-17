package com.jk.finice.authservice.controller;

import com.jk.finice.authservice.service.email.EmailService;
import com.jk.finice.authservice.entity.UserPrincipal;
import com.jk.finice.commonlibrary.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.jk.finice.commonlibrary.constants.AppConstants.AUTH_PATH;

@RestController
@RequestMapping(AUTH_PATH)
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationController {

    private final EmailService emailService;

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("[EMAIL-CONTROLLER] Email verification request received");

        emailService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully! You can now log in.")
        );
    }

    /**
     * Resend verification email (for logged-in users)
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("[EMAIL-CONTROLLER] Resend verification request for user: {}",
                principal.getUsername());

        emailService.resendVerificationEmail(principal.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Verification email sent. Please check your inbox.")
        );
    }

}
