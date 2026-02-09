package com.JK.FinIce.authservice.serviceTest;

import com.JK.FinIce.authservice.dto.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Manually build the validator factory to test annotations without Spring Context
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // --- Helper to build a valid request ---
    private RegisterRequest.RegisterRequestBuilder getValidRequestBuilder() {
        return RegisterRequest.builder()
                .username("ValidUser123")
                .email("test@example.com")
                .password("StrongP@ss1") // Meets all regex criteria
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+48123456789");
    }

    @Test
    void testValidRegisterRequest() {
        RegisterRequest request = getValidRequestBuilder().build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void testUsername_TooShort() {
        RegisterRequest request = getValidRequestBuilder()
                .username("ab") // Min is 3
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    void testUsername_InvalidPattern() {
        RegisterRequest request = getValidRequestBuilder()
                .username("User Name!") // Contains space and special char
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Username can only contain letters, numbers, underscores, and hyphens");
    }

    @Test
    void testEmail_InvalidFormat() {
        RegisterRequest request = getValidRequestBuilder()
                .email("invalid-email-format") // Missing @ and domain
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Invalid email format");
    }

    // --- Password Regex Tests ---

    @ParameterizedTest
    @ValueSource(strings = {
            "weakpass",           // No uppercase, no number, no special
            "WeakPass1",          // No special char
            "WeakPass!",          // No number
            "weak1!",             // No uppercase, too short
            "STRONGPASS1!"        // No lowercase
    })
    void testPassword_FailsPattern(String weakPassword) {
        RegisterRequest request = getValidRequestBuilder()
                .password(weakPassword)
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(msg -> msg.startsWith("Password must contain at least 8 characters"));
    }

    @Test
    void testPhoneNumber_Invalid() {
        RegisterRequest request = getValidRequestBuilder()
                .phoneNumber("12345") // Too short, missing + (optional but regex might require specific format)
                .build();

        // Regex: ^\+?[1-9]\d{1,14}$
        // "12345" matches the regex actually?
        // Let's test a string definitely invalid for E.164

        RegisterRequest requestInvalid = getValidRequestBuilder()
                .phoneNumber("invalid-phone")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(requestInvalid);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Invalid phone number format (E.164)");
    }
}
